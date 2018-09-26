package extensions.compgraph;

public class ArrayAccess {
  public static final int INTEGER_BIT_NUM = 32;
  public final String label;
  public int[] bitmap;
  public int begin = 0;

  public ArrayAccess(String label, int i, boolean write) {
    this.label = label;
		bitmap = new int[2];
    insert(i, write);
    begin = getBitmapIndex(i);
  }

  private int getBitmapIndex(int i) {
    return i / INTEGER_BIT_NUM * 2;
  }

  public void insert(int i, boolean write) {
    int index = getBitmapIndex(i);
    if (index < begin || index >= begin + bitmap.length)
      expand(index);
    if (!write)
      bitmap[index - begin] |= (1 << (i % INTEGER_BIT_NUM));
    else
      bitmap[index + 1 - begin] |= (1 << (i % INTEGER_BIT_NUM));
  }

  private void expand(int index) {
		int[] newBitmap = null;
		int newBegin = 0;
		int orgsize = bitmap.length;
		if (index < begin) {
			int extendedLength = bitmap.length;
			int gap = begin - index;
			// Make sure we always have enough space for incoming index
			if (extendedLength < gap) {
				extendedLength = gap;
			}
			// Make sure we won't reach anywhere below addr 0
			if (extendedLength > begin) {
				extendedLength = begin;
			}
			// Alloc new array and move old bitmap back
			newBitmap = new int[extendedLength + bitmap.length];
			System.arraycopy(bitmap, 0, newBitmap, extendedLength, bitmap.length);
			// Update head pointer
			begin -= extendedLength;
			bitmap = newBitmap;
		} else if (index >= begin + bitmap.length) {
			if (index >= begin + bitmap.length * 2) {
				newBitmap = new int[index + 2 - begin];
				System.arraycopy(bitmap, 0, newBitmap, 0, bitmap.length);
				bitmap = newBitmap;
			} else {
				newBitmap = new int[bitmap.length * 2];
				System.arraycopy(bitmap, 0, newBitmap, 0, bitmap.length);
				bitmap = newBitmap;
			}
		}
  }

  public boolean conflicts(ArrayAccess a) {
    if (!label.equals(a.label))
      return false;
    if (bitmap == null || a.bitmap == null)
      return false;
    int start = begin < a.begin ? a.begin : begin;
    int end = begin + bitmap.length < a.begin + a.bitmap.length
      ? begin + bitmap.length
      : a.begin + a.bitmap.length;
    int index1 = start - begin;
    int index2 = start - a.begin;
    int result = 0;
    for (int i = start; i < end; i += 2) {
      // read-write
      result = (bitmap[index1]) & (a.bitmap[index2 + 1]);
      // write-read
      result |= (bitmap[index1 + 1]) & (a.bitmap[index2]);
      // write-write
      result |= (bitmap[index1 + 1]) & (a.bitmap[index2 + 1]);
      if (result != 0)
        return true;
      index1 += 2;
      index2 += 2;
    }
    return false;
  }
}

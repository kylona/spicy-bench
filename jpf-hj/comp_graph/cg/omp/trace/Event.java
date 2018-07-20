package cg.omp.trace;

public class Event {
  private String name;

  public Event(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private Class<? extends Event> getLowestClass(Event event) {
    if(event instanceof Read) {
      return Read.class;
    } else if(event instanceof Write) {
      return Write.class;
    } else if(event instanceof Post) {
      return Post.class;
    } else if(event instanceof ThreadEvent) {
      return ThreadEvent.class;
    } else {
      return Event.class;
    }
  }

  public boolean isWrite() {
    return getLowestClass(this).equals(Write.class);
  }

  public boolean isRead() {
    return getLowestClass(this).equals(Read.class);
  }

  public boolean isPost() {
    return getLowestClass(this).equals(Post.class);
  }

  public boolean isThreadEvent() {
    return getLowestClass(this).equals(ThreadEvent.class);
  }
}


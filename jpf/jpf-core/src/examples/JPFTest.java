import java.util.Random;

public class JPFTest {

	public static void main(String[] args) {

		Random random = new Random(42);

		int a = random.nextInt(2);
		System.out.println(a);

		int b = random.nextInt(3);
		System.out.println(b);

		int c = a / (b + a - 2);
		System.out.println(c);
                
                System.out.println("happy");

	}

}

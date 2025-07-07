import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a URL: ");
            String inputUrl = scanner.nextLine();
            if (inputUrl.equals("exit")) {
                System.out.println("Exiting program...");
                break;
            }

            WebCrawler.crawl(inputUrl);
        }
    }
}

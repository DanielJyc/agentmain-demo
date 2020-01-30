import java.util.concurrent.TimeUnit;

/**
 * @description: app
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/18 8:30 PM
 * @version: 1.0.0
 */
public class ProducerApplication {

    public static void main(String[] args) {
        int count = 1;
        System.out.println("start execute main function.\n");
        while (true) {
            produceProduct(new ProductModel(count++, "pcode", "pname"));
        }
    }

    private static void produceProduct(ProductModel productModel) {
        System.out.println("生产完成" + productModel.getId() + " 件。");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
        }
    }
}

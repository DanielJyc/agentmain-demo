/**
 * @description: 产品
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/21 3:34 PM
 * @version: 1.0.0
 */
public class ProductModel {
    public ProductModel(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    private int id;
    private String code;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProductModel{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

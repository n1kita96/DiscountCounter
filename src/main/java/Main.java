import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author Mykyta Shvets
 */
public class Main {
    public static void main(String args[] ) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String input = scanner.nextLine();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])*");

        Cart cart = new Cart();

        /*parsing json to values in models*/
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(input);
        JSONObject jsonObject = (JSONObject) obj;
        //setting values for cart model from input
        cart.setId((Long)jsonObject.get("id"));
        cart.setDiscountType(DiscountType.valueOf(((String)jsonObject.get("discount_type")).toUpperCase()));
        cart.setDiscountValue((Double)jsonObject.get("discount_value"));
        if ((String)jsonObject.get("collection") != null ) {
            cart.setKeyType(KeyType.COLLECTION);
            cart.setKeyValue((String)jsonObject.get("collection"));
        } else if ((Double)jsonObject.get("product_value") != null) {
            cart.setKeyType(KeyType.PRODUCT_VALUE);
            cart.setKeyValue(((Double)jsonObject.get("product_value")).toString());
        } else if ((Double)jsonObject.get("cart_value") != null) {
            cart.setKeyType(KeyType.CART_VALUE);
            cart.setKeyValue(((Double)jsonObject.get("cart_value")).toString());
        }

        //total products handled
        int total = 0;
        Pagination pagination = new Pagination();
        //read data for all availavle products in the cart
        while(total <= pagination.getTotal()) {
            //start with page #1
            pagination.setCurrentPage(pagination.getCurrentPage()+1);
            //get data from API
            String cartInfo = UrlUtils.callURL("domain/carts.json?id=" + cart.getId() + "&page=" + pagination.getCurrentPage());
            obj = parser.parse(cartInfo);
            jsonObject = (JSONObject) obj;
            //get data about pagination
            JSONObject paginationJsonObject = (JSONObject) jsonObject.get("pagination");
            pagination.setPerPage(((Long)paginationJsonObject.get("per_page")).intValue());
            pagination.setTotal(((Long)paginationJsonObject.get("total")).intValue());
            //get data about products
            JSONArray products = (JSONArray) jsonObject.get("products");
            Iterator<JSONObject> iterator = products.iterator();
            //put all products in cart in product list
            while (iterator.hasNext()) {
                jsonObject = iterator.next();
                //create new product with name and price
                Product product = new Product((String) jsonObject.get("name"), (Double) jsonObject.get("price"));
                //set collection for current product if exists
                if ((String)jsonObject.get("collection") != null) {
                    product.setCollection((String)jsonObject.get("collection"));
                }
                //and put the product into the list
                cart.getProducts().add(product);
            }
            //add amount of products, that were read from current page to 'total'
            total += pagination.getPerPage();
        }

        //if discount by collection
        if (cart.getKeyType() == KeyType.COLLECTION) {
            for (Product p : cart.getProducts()) {
                //count total amount
                cart.setTotalAmount(cart.getTotalAmount() + p.getPrice());
                //check if discount collection match with actual collection of product
                if (p.getCollection() != null && p.getCollection().equals(cart.getKeyValue())){
                    //price after discount have not be less than 0
                    if (p.getPrice() - cart.getDiscountValue() >= 0) {
                        cart.setAmountAfterDiscount(cart.getAmountAfterDiscount() + p.getPrice() - cart.getDiscountValue());
                    }
                } else {
                    //if collection of the product doesn't match with discount collection just add to amount price without any discounts
                    cart.setAmountAfterDiscount(cart.getAmountAfterDiscount() + p.getPrice());
                }
            }
            //if discount by product type
        } else if (cart.getKeyType() == KeyType.PRODUCT_VALUE) {
            for (Product p : cart.getProducts()) {
                //count total amount
                cart.setTotalAmount(cart.getTotalAmount() + p.getPrice());
                //count discount if available
                if (p.getPrice() >= Double.valueOf(cart.getKeyValue())){
                    if (p.getPrice() - cart.getDiscountValue() >= 0) {
                        cart.setAmountAfterDiscount(cart.getAmountAfterDiscount() + p.getPrice() - cart.getDiscountValue());
                    }
                } else {
                    //if discount not available for this product just add current price without any discounts
                    cart.setAmountAfterDiscount(cart.getAmountAfterDiscount() + p.getPrice());
                }
            }
            //if discount by cart
        } else if (cart.getKeyType() == KeyType.CART_VALUE) {
            for (Product p : cart.getProducts()) {
                //count total amount
                cart.setTotalAmount(cart.getTotalAmount() + p.getPrice());
            }
            //check if discount acceptable for this cart
            if (cart.getTotalAmount() >= Double.valueOf(cart.getKeyValue())){
                cart.setAmountAfterDiscount(cart.getTotalAmount() - cart.getDiscountValue());
            } else {
                cart.setAmountAfterDiscount(cart.getTotalAmount());
            }
        }

        //amount in cart after discount should not be less than 0
        if(cart.getAmountAfterDiscount() < 0) {
            cart.setAmountAfterDiscount(0);
        }

        //linked hash map for saving order
        Map result = new LinkedHashMap();
        result.put("total_amount", cart.getTotalAmount());
        result.put("total_after_discount", cart.getAmountAfterDiscount());
        JSONObject json = new JSONObject(result);

        System.out.println(json.toJSONString());
    }
}

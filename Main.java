import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    private static final List<MenuItem> MENU = List.of(
            new MenuItem(1, "Margherita Pizza", "Pizza", 249.00),
            new MenuItem(2, "Paneer Tikka Pizza", "Pizza", 329.00),
            new MenuItem(3, "Veg Burger", "Burger", 149.00),
            new MenuItem(4, "Chicken Burger", "Burger", 189.00),
            new MenuItem(5, "Masala Dosa", "South Indian", 129.00),
            new MenuItem(6, "Veg Biryani", "Biryani", 199.00),
            new MenuItem(7, "Chicken Biryani", "Biryani", 259.00),
            new MenuItem(8, "Cold Coffee", "Beverage", 99.00),
            new MenuItem(9, "Fresh Lime Soda", "Beverage", 79.00)
    );

    public static void main(String[] args) {
        Cart cart = new Cart();
        System.out.println("Welcome to QuickBite Food Ordering System");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> showMenu();
                case 2 -> addItemToCart(cart);
                case 3 -> removeItemFromCart(cart);
                case 4 -> viewCart(cart);
                case 5 -> running = checkout(cart);
                case 0 -> {
                    running = false;
                    System.out.println("Thank you for visiting QuickBite.");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("1. View menu");
        System.out.println("2. Add item to cart");
        System.out.println("3. Remove item from cart");
        System.out.println("4. View cart");
        System.out.println("5. Checkout");
        System.out.println("0. Exit");
    }

    private static void showMenu() {
        System.out.println();
        System.out.println("Available Food Items");
        System.out.println("-----------------------------------------------");
        for (MenuItem item : MENU) {
            System.out.printf("%2d. %-22s %-13s %8s%n",
                    item.id(), item.name(), item.category(), money(item.price()));
        }
    }

    private static void addItemToCart(Cart cart) {
        showMenu();
        int itemId = readInt("Enter item number: ");
        Optional<MenuItem> selectedItem = MENU.stream()
                .filter(item -> item.id() == itemId)
                .findFirst();

        if (selectedItem.isEmpty()) {
            System.out.println("No menu item found for that number.");
            return;
        }

        int quantity = readInt("Enter quantity: ");
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        cart.add(selectedItem.get(), quantity);
        System.out.println(quantity + " x " + selectedItem.get().name() + " added to cart.");
    }

    private static void removeItemFromCart(Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        viewCart(cart);
        int itemId = readInt("Enter item number to remove: ");
        boolean removed = cart.remove(itemId);
        System.out.println(removed ? "Item removed from cart." : "That item is not in your cart.");
    }

    private static void viewCart(Cart cart) {
        System.out.println();
        System.out.println("Your Cart");
        System.out.println("-----------------------------------------------");

        if (cart.isEmpty()) {
            System.out.println("No items added yet.");
            return;
        }

        for (CartItem item : cart.items()) {
            System.out.printf("%2d. %-22s x%-3d %8s%n",
                    item.menuItem().id(),
                    item.menuItem().name(),
                    item.quantity(),
                    money(item.subtotal()));
        }

        System.out.println("-----------------------------------------------");
        System.out.printf("Subtotal:%34s%n", money(cart.subtotal()));
        System.out.printf("GST 5%%:%36s%n", money(cart.tax()));
        System.out.printf("Delivery:%34s%n", money(cart.deliveryFee()));
        System.out.printf("Total:%37s%n", money(cart.total()));
    }

    private static boolean checkout(Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Add at least one item before checkout.");
            return true;
        }

        viewCart(cart);
        String name = readText("Customer name: ");
        String phone = readText("Phone number: ");
        String address = readText("Delivery address: ");

        System.out.println();
        System.out.println("Order Confirmed");
        System.out.println("-----------------------------------------------");
        System.out.println("Customer: " + name);
        System.out.println("Phone: " + phone);
        System.out.println("Address: " + address);
        System.out.println("Amount payable: " + money(cart.total()));
        System.out.println("Estimated delivery time: 30-40 minutes");
        System.out.println("Thank you for ordering with QuickBite.");
        return false;
    }

    private static String money(double amount) {
        return "INR " + String.format("%.2f", amount);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }
}

record MenuItem(int id, String name, String category, double price) {
}

record CartItem(MenuItem menuItem, int quantity) {
    double subtotal() {
        return menuItem.price() * quantity;
    }

    CartItem withAddedQuantity(int extraQuantity) {
        return new CartItem(menuItem, quantity + extraQuantity);
    }
}

class Cart {
    private static final double GST_RATE = 0.05;
    private static final double DELIVERY_FEE = 40.00;

    private final List<CartItem> items = new ArrayList<>();

    void add(MenuItem menuItem, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            CartItem existing = items.get(i);
            if (existing.menuItem().id() == menuItem.id()) {
                items.set(i, existing.withAddedQuantity(quantity));
                return;
            }
        }
        items.add(new CartItem(menuItem, quantity));
    }

    boolean remove(int itemId) {
        return items.removeIf(item -> item.menuItem().id() == itemId);
    }

    List<CartItem> items() {
        return List.copyOf(items);
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    double subtotal() {
        return items.stream()
                .mapToDouble(CartItem::subtotal)
                .sum();
    }

    double tax() {
        return subtotal() * GST_RATE;
    }

    double deliveryFee() {
        return isEmpty() ? 0.00 : DELIVERY_FEE;
    }

    double total() {
        return subtotal() + tax() + deliveryFee();
    }
}

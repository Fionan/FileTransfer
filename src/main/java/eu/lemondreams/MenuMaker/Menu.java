package eu.lemondreams.MenuMaker;

import static java.lang.System.out;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Scanner;

public class Menu {

    static final String  DEFAULT_MENU_NAME = "Menu";
    static final String TICK = "\u2713";

    public enum InputType {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        DATE,
        FILE,
        MULTI
    }

    public enum MenuType {
        MAIN,
        SUBMENU

    }

    public interface MenuChangeListener {
        void onMenuChanged(String key);
    }



    private Menu parentMenu;
    private List<MenuItem> menuItems;
    private static Scanner scanner = new Scanner(System.in);
    private static final Map<String, String> Global_Values= new HashMap<>();
    private String menuName = DEFAULT_MENU_NAME;
 


    public Menu(MenuType menuType){
        this.parentMenu=null;
        this.menuItems = new ArrayList<>();


    }


    public Menu(Menu parentMenu) {
        this.parentMenu = parentMenu;
        this.menuItems = new ArrayList<>();

   
    }

    public Menu(MenuType menuType, Menu parentMenu) {
        this.parentMenu = parentMenu;
        this.menuItems = new ArrayList<>();

    }


    public Menu(Menu parentMenu, String menuName) {
        this.parentMenu = parentMenu;
        this.menuItems = new ArrayList<>();

        this.menuName = menuName;
    
    }



    public void addMenuItem(MenuItem menuItem) {
        menuItems.add(menuItem);
    }


    public void addMenuItem(String description,Menu submenu){
        menuItems.add(new MenuItem(description,submenu));


    }

    public void addMenuItem(String description,Runnable function){
        menuItems.add(new MenuItem(description,function));

    }


    public void setValue(String key, String value) {
        Global_Values.put(key, value);

    }


    public String getValue(String key) {
        return Global_Values.get(key);
    }
      public Map getValues() {
        return Global_Values;
    }


    public boolean containsValue(String s) {
        return Global_Values.containsKey(s);
    }
    public boolean containsKeyValues(String... values) {
        for(String s: values) {

         if (!this.containsValue(s)){return false;}

        }


        return true;
    }

    private void displayMenu() {
        clearScreen();
        out.println("\n--- " + this.menuName + " ---");

        List<MenuItem> visibleMenuItems = getVisibleMenuItems();

        for (int i = 0; i < visibleMenuItems.size(); i++) {

            MenuItem mi = visibleMenuItems.get(i);
            out.println((i + 1) + ". " + mi.getDescription()+completed(mi));
        }

        int lastIndex = visibleMenuItems.size() + 1;
        if (parentMenu != null) {
            out.println(lastIndex + ". Back");
        } else {
            out.println(lastIndex + ". Exit");
        }

        out.print("Enter your choice: ");
    }

    private List<MenuItem> getVisibleMenuItems() {
        List<MenuItem> visibleMenuItems = new ArrayList<>();
        for (MenuItem menuItem : menuItems) {
            if (menuItem.checkMenuCondition()) {
                visibleMenuItems.add(menuItem);
            }
        }
        return visibleMenuItems;
    }
    public void run() {
        boolean exit = false;
        while (!exit) {

            displayMenu();
            int choice = readUserChoice();

            List<MenuItem> visibleMenuItems = getVisibleMenuItems();

            if (choice >= 1 && choice <= visibleMenuItems.size()) {
                int actualChoice = getActualChoice(choice, visibleMenuItems.size());
                MenuItem menuItem = visibleMenuItems.get(actualChoice - 1);
                if (menuItem.hasSubMenu()) {
                    System.out.println(this.menuName);
                    Menu subMenu = menuItem.getSubMenu();
                    subMenu.run();
                } else {
                    System.out.println(this.menuName);
                    Runnable action = menuItem.getAction();
                    if (action != null) {
                        action.run();
                    }
                }
            } else if (choice == visibleMenuItems.size() + 1) {
                if (parentMenu != null) {
                    exit = true;  // Go back
                } else {
                    exit = true;  // Exit the application
                }
            }
        }
    }


    private int getActualChoice(int displayedChoice, int maxVisibleChoice) {
        if (displayedChoice >= 1 && displayedChoice <= maxVisibleChoice) {
            return displayedChoice;
        }
        // If the displayed choice is not within valid range, return -1 or handle accordingly
        return -1;
    }
    private String completed(MenuItem mi){
    //part of Cosmetic , simply adding a tick if this is marked as completed
        if(mi.isCompleted())return " " + TICK;
        return "";
    };

    public void close() {
        scanner.close();
    }

    public String getUserInputString() {
        return scanner.nextLine();
    }

    public int getUserInputInt() {
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            out.println("Invalid input. Please enter a valid number.");
            out.print("Enter your choice: ");
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        return choice;
    }

    public double getUserInputDouble() {
        while (!scanner.hasNextDouble()) {
            scanner.nextLine();
            out.println("Invalid input. Please enter a valid number.");
            out.print("Enter your choice: ");
        }
        double choice = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character
        return choice;
    }

    public boolean getUserInputBoolean() {
        String input = scanner.nextLine().toLowerCase();
        switch (input) {
            case "true":
            case "t":
                return true;
            case "false":
            case "f":
                return false;
            default:
                out.println("Invalid input. Please enter 'true' or 'false'.");
                return getUserInputBoolean();
        }
    }

    public Date getUserInputDate() {
        out.print("Enter date (DD-MM-YYYY): ");
        String dateString = scanner.nextLine();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            out.println("Invalid date format. Please enter a date in the format DD-MM-YYYY.");
            return getUserInputDate();
        }
    }

    public File getUserInputFile() {
        out.print("Enter file path or select a file: ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        while (!file.exists()) {
            out.println("Invalid file path. Please enter a valid file path or select a file.");
            out.println("You wrote: "+filePath);
            out.print("Enter file path or select a file: ");
            filePath = scanner.nextLine();
            file = new File(filePath);
        }
        return file;
    }

    public void addMenuItemWithKVInput(InputType expectedType, String description, String key) {
        MenuItem menuItem = new MenuItem("Set " + description, () -> {
            out.print("Enter " + description + ": ");
            String value = switch (expectedType) {
                case STRING -> getUserInputString();
                case INTEGER -> Integer.toString(getUserInputInt());
                case DOUBLE -> Double.toString(getUserInputDouble());
                case BOOLEAN -> Boolean.toString(getUserInputBoolean());
                case DATE -> getUserInputDate().toString();
                case FILE -> getUserInputFile().getPath();
                default -> getUserInputString();
            };
            out.println("Adding "+value+"With key "+key);
            setValue(key, value);

            out.println(getValue(key));
        });
        addMenuItem(menuItem);
    }
    public void addMenuItemWithMultipleKVInput(InputType expectedType, String description, List<String> keys) {
        MenuItem menuItem = new MenuItem("Set " + description, () -> {
            for (String key : keys) {
                out.print("Enter " + description + " for key '" + key + "': ");
                String value = switch (expectedType) {
                    case STRING -> getUserInputString();
                    case INTEGER -> Integer.toString(getUserInputInt());
                    case DOUBLE -> Double.toString(getUserInputDouble());
                    case BOOLEAN -> Boolean.toString(getUserInputBoolean());
                    case DATE -> getUserInputDate().toString();
                    case FILE -> getUserInputFile().getPath();
                    default -> getUserInputString();
                };
                out.println("Adding " + value + " with key " + key);
                setValue(key, value);
            }

            // Optionally, you can print all the entered values
            for (String key : keys) {
                out.println("Key '" + key + "' has value: " + getValue(key));

            }
        });

        addMenuItem(menuItem);
    }








    private int readUserChoice() {
        return getUserInputInt();
    }


    //Getters and Setters Start:

    /**
     * @return Menu return the parentMenu
     */
    public Menu getParentMenu() {
        return parentMenu;
    }

    /**
     * @param parentMenu the parentMenu to set
     */
    public void setParentMenu(Menu parentMenu) {
        this.parentMenu = parentMenu;
    }

    /**
     * @return List<MenuItem> return the menuItems
     */
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    /**
     * @param menuItems the menuItems to set
     */
    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * @return Scanner return the scanner
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     * @param scanner the scanner to set
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * @return String return the menuName
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * @param menuName the menuName to set
     */
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }


    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}


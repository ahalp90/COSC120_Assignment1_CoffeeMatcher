# Java Bean Order Genie

This project is a Java desktop application created for the COSC120 course at the University of New England. It provides a graphical user interface (GUI) for customers to find their perfect coffee. The application features a custom "coffee matcher" that recommends drinks based on user preferences, as well as options to browse the full menu and place an order directly. Finalised orders are saved as individual `.txt` files.

## Features

*   **Interactive GUI:** A user-friendly interface built with Java Swing's `JOptionPane` for all interactions.
*   **Custom Coffee Matcher:** Allows users to specify their ideal coffee by criteria such as drink type, milk, number of shots, sugar, provenance, extras, and price range.
*   **Personalised Recommendations:** Displays a list of coffees from the menu that match the user's customised preferences.
*   **Full Menu Browser:** A scrollable view that displays detailed information for every coffee on the menu.
*   **Direct Ordering System:** Users can select any item directly from the menu and customise its attributes (like milk and extras) before ordering.
*   **Order Generation:** Collects customer details (name, email, phone number) and generates a formatted order summary in a unique `.txt` file (e.g., `Order_0400133700_1.txt`).
*   **Robust Data Parsing:** Reads and validates coffee data from an external `menu.txt` file, with clear error handling for invalid entries.

## Getting Started

To run this application, you will need a Java Development Kit (JDK) and the required project files.

### Prerequisites

*   Java Development Kit (JDK), version 17 or higher.
*   The following files must be present in the project's root directory:
    *   `menu.txt` (The coffee menu data file)
    *   `java_bean.jpg` (The icon used in the GUI dialogues)

### Running the Application

1.  Place all `.java` source files, `menu.txt`, and `java_bean.jpg` into the same directory.
2.  Open a terminal or command prompt and navigate to that directory.
3.  Compile all the Java source files using the `javac` command:
    ```sh
    javac *.java
    ```
4.  Run the application by executing the main class, `MenuSearcher`:
    ```sh
    java MenuSearcher
    ```

## How to Use

Upon launching, the application presents a main menu with several options to guide you through the ordering process.

1.  **View the Full Menu:** Displays a complete, scrollable list of all available coffees and their detailed descriptions, ingredients, and prices.

2.  **Describe my Ideal Coffee:** This starts the coffee matching process. You will be prompted to enter your preferences through a series of dialogues:
    *   **Drink Type:** Hot Coffee or Frappe.
    *   **Milk Choice:** A selection of dairy and non-dairy options.
    *   **Price Range:** Your minimum and maximum budget.
    *   **Extras:** Select one or more extras (e.g., Whipped Cream, Caramel Syrup). You can also choose to skip this step to see matches regardless of extras.
    *   **Provenance:** The origin of the coffee beans.
    *   **Number of Shots:** The desired number of espresso shots.
    *   **Sugar:** A simple Yes/No choice.

3.  **View my Coffee Matches and Order:** After describing your ideal coffee, select this option.
    *   A new window will display all the coffees that match your criteria.
    *   If no matches are found, you will be notified.
    *   You can select a coffee from the matching list using a drop-down menu to proceed with an order.

4.  **Order Any Item Off the Menu:** If you already know what you want, this option allows you to:
    *   Select any coffee from an alphabetised list of all menu items.
    *   Customise your selection by choosing from the available milk and extras for that specific coffee.

5.  **Placing the Order:** Once a coffee is selected for purchase (either through the matcher or direct ordering), you will be asked to provide your details:
    *   Full Name
    *   Email Address
    *   Phone Number

After confirming your details, the application will generate an order file (e.g., `Order_0400133700_1.txt`) in the root directory and display a final confirmation dialogue with your order summary.

## Project Structure

The project is organised into several classes, each with a specific responsibility.

*   `MenuSearcher.java`: The main class containing the `main` method. It manages the application's overall flow, handles all GUI interactions, and coordinates the ordering process.
*   `Coffee.java`: A class that represents a single coffee item. It stores all attributes like name, price, ingredients, and description.
*   `Menu.java`: A class that holds the entire collection of `Coffee` objects loaded from `menu.txt`. It contains the core `coffeeMatcher` logic for finding coffees based on user preferences.
*   `DrinkType.java`, `Milk.java`, `Provenance.java`: Enumerations (`enum`) that define the fixed sets of options for coffee type, milk, and bean origin, ensuring type safety and consistency.
*   `Geek.java`: A `record` used as a simple data carrier for the customer's personal details (name, email, phone).
*   `Order.java`: A `record` that encapsulates all the necessary information for a final order before it is written to a text file.

## `menu.txt` File Format

The application parses its coffee data from `menu.txt`. The file must follow a specific comma-separated format. The first line is treated as a header and is ignored. Each subsequent line represents one coffee and must contain exactly 10 fields in the following order:

1.  **Menu Item ID** (String, e.g., `C001`)
2.  **Menu Item Name** (String, e.g., `Classic Latte`)
3.  **Price** (Float, e.g., `4.50`)
4.  **Number of Shots** (Integer, e.g., `2`)
5.  **Sugar** (String: `Yes` or `No`, case-insensitive)
6.  **Drink Type** (String, e.g., `Hot Coffee`)
7.  **Provenance** (String, e.g., `Ethiopian Superior`)
8.  **Milk Options** (A comma-separated list inside square brackets, e.g., `[Full-cream, Skim, Soy]`)
9.  **Extras Options** (A comma-separated list inside square brackets, e.g., `[Caramel Syrup, Extra Shot]`)
10. **Description** (A descriptive string, e.g., `A smooth and creamy coffee classic.`)

**Example Line:**
```
C001,Classic Latte,4.50,2,No,Hot Coffee,Ethiopian Superior,[Full-cream, Skim, Soy, Almond],[Caramel Syrup, Extra Shot],A smooth and creamy coffee classic.
```

## Author

*   **Ariel Halperin** - *Initial work for COSC120, Assignment 1*

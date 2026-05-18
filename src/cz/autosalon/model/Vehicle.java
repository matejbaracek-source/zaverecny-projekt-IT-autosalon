package cz.autosalon.model;

/**
 * Třída reprezentující vozidlo v autosalonu.
 * (Class representing a vehicle in the car salon.)
 */
public class Vehicle {
    private String id;
    private String brand;
    private String model;
    private int year;
    private double price;
    private String description;

    /**
     * Konstruktor pro vytvoření nového vozidla.
     */
    public Vehicle(String id, String brand, String model, int year, double price, String description) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
        this.description = description;
    }

    // Gettery a settery (Getters and Setters)
    // Slouží k přístupu k soukromým proměnným třídy.

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ") - " + price + " Kč";
    }
}

package cz.autosalon.logic;

import cz.autosalon.model.Vehicle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Třída pro správu inventáře vozidel.
 * (Class for managing the vehicle inventory.)
 */
public class Inventory {
    private List<Vehicle> vehicles;

    public Inventory() {
        this.vehicles = DataStorage.load();
        if (this.vehicles.isEmpty()) {
            // Přidáme nějaká testovací data (Initial test data)
            seedData();
            DataStorage.save(this.vehicles);
        }
    }

    private void seedData() {
        vehicles.add(new Vehicle("1", "Škoda", "Octavia", 2022, 650000, "Zánovní vůz, servisní knížka."));
        vehicles.add(new Vehicle("2", "Volkswagen", "Golf", 2020, 450000, "Spolehlivý hatchback."));
        vehicles.add(new Vehicle("3", "Hyundai", "i30", 2023, 550000, "Nový vůz, plná záruka."));
        vehicles.add(new Vehicle("4", "BMW", "3", 2018, 590000, "Sportovní sedan, bohatá výbava."));
    }

    // CRUD Operace

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        DataStorage.save(vehicles);
    }

    public void removeVehicle(String id) {
        vehicles.removeIf(v -> v.getId().equals(id));
        DataStorage.save(vehicles);
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    // Filtrování (Filtering)

    public List<Vehicle> filterVehicles(String brand, Double maxPrice, Integer year) {
        return vehicles.stream()
                .filter(v -> (brand == null || brand.isEmpty() || v.getBrand().toLowerCase().contains(brand.toLowerCase())))
                .filter(v -> (maxPrice == null || v.getPrice() <= maxPrice))
                .filter(v -> (year == null || v.getYear() == year))
                .collect(Collectors.toList());
    }

    // Statistiky (Statistics)

    public int getTotalCount() {
        return vehicles.size();
    }

    public double getTotalValue() {
        return vehicles.stream().mapToDouble(Vehicle::getPrice).sum();
    }
}


package storage;

import model.Clothing;
import model.Good;
import model.Sale;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileStorage {
    private final Path filePath;

    public FileStorage() {
        filePath = Path.of("data", "inventory.txt");
    }

    public static class Data {
        public final List<Good> goods = new ArrayList<>();
        public final List<Sale> sales = new ArrayList<>();
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
            value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String decode(String value) {
        return new String(
            Base64.getDecoder().decode(value),
            StandardCharsets.UTF_8
        );
    }

    public Data load() throws IOException {
        Data data = new Data();

        if (!Files.exists(filePath)) {
            return data;
        }

        List<String> lines = Files.readAllLines(
            filePath, StandardCharsets.UTF_8
        );

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);

            if (line.isBlank()) {
                continue;
            }

            try {
                String[] fields = line.split("\\|", -1);

                if (fields[0].equals("G")) {
                    loadGood(fields, data);
                } else if (fields[0].equals("S")) {
                    loadSale(fields, data);
                } else {
                    throw new IllegalArgumentException(
                        "Unknown record type."
                    );
                }
            } catch (RuntimeException ex) {
                throw new IOException(
                    "Invalid data on line " + (lineNumber + 1)
                        + " of " + filePath,
                    ex
                );
            }
        }

        return data;
    }

    private void loadGood(String[] f, Data data) {
        if (f.length != 11) {
            throw new IllegalArgumentException(
                "Invalid inventory record."
            );
        }

        int id = Integer.parseInt(f[1]);
        String type = f[2];
        String name = decode(f[3]);
        String brand = decode(f[4]);
        int quantity = Integer.parseInt(f[5]);
        double cost = Double.parseDouble(f[6]);
        LocalDate dateListed = LocalDate.parse(f[7]);

        Good good;

        if (type.equals("C")) {
            good = new Clothing(
                id, name, brand, quantity, cost, dateListed,
                decode(f[8]), decode(f[9]), decode(f[10])
            );
        } else if (type.equals("G")) {
            good = new Good(
                id, name, brand, quantity, cost, dateListed
            );
        } else {
            throw new IllegalArgumentException(
                "Unknown inventory item type."
            );
        }

        data.goods.add(good);
    }

    private void loadSale(String[] f, Data data) {
        if (f.length != 10) {
            throw new IllegalArgumentException(
                "Invalid sale record."
            );
        }

        Sale sale = new Sale(
            Integer.parseInt(f[1]),
            decode(f[2]),
            LocalDate.parse(f[3]),
            Integer.parseInt(f[4]),
            Double.parseDouble(f[5]),
            Double.parseDouble(f[6]),
            Double.parseDouble(f[7]),
            Double.parseDouble(f[8]),
            Double.parseDouble(f[9])
        );

        data.sales.add(sale);
    }

    public void save(List<Good> goods, List<Sale> sales)
        throws IOException {

        List<String> lines = new ArrayList<>();

        for (Good good : goods) {
            String type = "G";
            String size = "";
            String color = "";
            String category = "";

            if (good instanceof Clothing clothing) {
                type = "C";
                size = clothing.getSize();
                color = clothing.getColor();
                category = clothing.getCategory();
            }

            lines.add(String.join("|",
                "G",
                String.valueOf(good.getId()),
                type,
                encode(good.getName()),
                encode(good.getBrand()),
                String.valueOf(good.getQuantity()),
                String.valueOf(good.getCost()),
                good.getDateListed().toString(),
                encode(size),
                encode(color),
                encode(category)
            ));
        }

        for (Sale sale : sales) {
            lines.add(String.join("|",
                "S",
                String.valueOf(sale.getItemId()),
                encode(sale.getItemName()),
                sale.getDateSold().toString(),
                String.valueOf(sale.getQuantitySold()),
                String.valueOf(sale.getItemCostAtSale()),
                String.valueOf(sale.getSoldPrice()),
                String.valueOf(sale.getShippingCharged()),
                String.valueOf(sale.getShippingCost()),
                String.valueOf(sale.getFees())
            ));
        }

        Files.createDirectories(filePath.getParent());

        Path tempPath = filePath.resolveSibling("inventory.tmp");

        Files.write(
            tempPath,
            lines,
            StandardCharsets.UTF_8
        );

        try {
            Files.move(
                tempPath,
                filePath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE
            );
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(
                tempPath,
                filePath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}

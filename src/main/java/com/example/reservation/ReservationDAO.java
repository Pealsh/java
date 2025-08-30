package com.example.reservation; 
 
import java.io.*; 
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter; 
import java.time.format.DateTimeParseException; 
import java.util.ArrayList; 
import java.util.Comparator; 
import java.util.List; 
import java.util.concurrent.CopyOnWriteArrayList; 
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.stream.Collectors; 
 
public class ReservationDAO { 
    private static final List<Reservation> reservations = new CopyOnWriteArrayList<>(); 
    private static final AtomicInteger idCounter = new AtomicInteger(0); 
    private static final String DATA_FILE = "reservations.dat"; 
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; 
 
    static { 
        loadReservations(); 
    } 
 
    public List<Reservation> getAllReservations() { 
        return new ArrayList<>(reservations); 
    } 
 
    public Reservation getReservationById(int id) { 
        return reservations.stream() 
                .filter(r -> r.getId() == id) 
                .findFirst() 
                .orElse(null); 
    } 
 
    public boolean addReservation(String name, LocalDateTime reservationTime) { 
        if (isDuplicate(name, reservationTime)) { 
            return false; 
        } 
        int id = idCounter.incrementAndGet(); 
        reservations.add(new Reservation(id, name, reservationTime)); 
        saveReservations(); 
        return true; 
    }
    public boolean updateReservation(int id, String name, LocalDateTime reservationTime) { 
        if (isDuplicate(name, reservationTime, id)) { 
            return false; 
        } 
        for (int i = 0; i < reservations.size(); i++) { 
            if (reservations.get(i).getId() == id) { 
                reservations.set(i, new Reservation(id, name, reservationTime)); 
                saveReservations(); 
                return true; 
            } 
        } 
        return false; 
    } 
 
    public boolean deleteReservation(int id) { 
        boolean removed = reservations.removeIf(r -> r.getId() == id); 
        if (removed) { 
            saveReservations(); 
        } 
        return removed; 
    } 
 
    public void cleanUpPastReservations() { 
        int initialSize = reservations.size(); 
        reservations.removeIf(r -> r.getReservationTime().isBefore(LocalDateTime.now())); 
        if (reservations.size() < initialSize) { 
            saveReservations(); 
        } 
    } 
 
    public List<Reservation> searchAndSortReservations(String searchTerm, String sortBy, String sortOrder) { 
        List<Reservation> filteredList = reservations.stream()
                .filter(r -> searchTerm == null || searchTerm.trim().isEmpty() ||
                        r.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        r.getReservationTime().format(FORMATTER).contains(searchTerm))
                .collect(Collectors.toList()); 
 
        Comparator<Reservation> comparator = null; 
        if ("name".equals(sortBy)) { 
            comparator = Comparator.comparing(Reservation::getName); 
        } else if ("time".equals(sortBy)) { 
            comparator = Comparator.comparing(Reservation::getReservationTime); 
        } 
 
        if (comparator != null) { 
            if ("desc".equals(sortOrder)) { 
                filteredList.sort(comparator.reversed()); 
            } else { 
                filteredList.sort(comparator); 
            } 
        } 
        return filteredList; 
    } 
 
    public void importReservations(BufferedReader reader) throws IOException { 
        String line; 
        boolean isFirstLine = true;
        int importedCount = 0;
        int skippedCount = 0;
        
        while ((line = reader.readLine()) != null) { 
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (isFirstLine && (line.contains("ID") || line.contains("名前") || line.contains("予約日時"))) {
                isFirstLine = false;
                continue;
            }
            isFirstLine = false;
            
            String[] parts = line.split(",");
            if (parts.length >= 2) { 
                try {
                    String name;
                    LocalDateTime time;
                    
                    if (parts.length == 2) {
                        // ID無しの場合：名前,日時
                        name = parts[0].trim();
                        time = parseDateTime(parts[1].trim());
                    } else {
                        // ID有りの場合：ID,名前,日時
                        name = parts[1].trim();
                        time = parseDateTime(parts[2].trim());
                    }
                    
                    if (!isDuplicate(name, time)) { 
                        int newId = idCounter.incrementAndGet();
                        reservations.add(new Reservation(newId, name, time)); 
                        importedCount++;
                    } else {
                        skippedCount++;
                        System.out.println("重複のためスキップ: " + name + " - " + time);
                    }
                } catch (Exception e) { 
                    skippedCount++;
                    System.err.println("無効なCSV行をスキップ: " + line + " - " + e.getMessage()); 
                } 
            } else {
                skippedCount++;
                System.err.println("不正なフォーマットの行をスキップ: " + line);
            }
        } 
        
        System.out.println("CSVインポート完了: " + importedCount + "件追加, " + skippedCount + "件スキップ");
        saveReservations(); 
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm",           // 2023-12-25T14:30
            "yyyy-MM-dd HH:mm",             // 2023-12-25 14:30  
            "yyyy/MM/dd HH:mm",             // 2023/12/25 14:30
            "yyyy-MM-dd'T'HH:mm:ss"         // 2023-12-25T14:30:00
        };
        
        for (String format : formats) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException e) {

            }
        }
        

        throw new DateTimeParseException("サポートされていない日時フォーマット: " + dateTimeStr, dateTimeStr, 0);
    } 
 
    private boolean isDuplicate(String name, LocalDateTime time) { 
        return reservations.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(name) &&
                        r.getReservationTime().equals(time)); 
    } 
 
    private boolean isDuplicate(String name, LocalDateTime time, int excludeId) { 
        return reservations.stream()
                .anyMatch(r -> r.getId() != excludeId && r.getName().equalsIgnoreCase(name) &&
                        r.getReservationTime().equals(time)); 
    } 
 
    private static void saveReservations() { 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) { 
            for (Reservation res : reservations) { 
                writer.write(String.format("%d,%s,%s%n", res.getId(), res.getName(),
                        res.getReservationTime().format(FORMATTER))); 
            } 
        } catch (IOException e) { 
            System.err.println("Error saving reservations: " + e.getMessage()); 
        } 
    } 
 
    private static void loadReservations() { 
        File file = new File(DATA_FILE); 
        if (!file.exists()) { 
            return; 
        } 
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) { 
            String line; 
            int maxId = 0; 
            while ((line = reader.readLine()) != null) { 
                String[] parts = line.split(","); 
                if (parts.length == 3) { 
                    try { 
                        int id = Integer.parseInt(parts[0]); 
                        String name = parts[1]; 
                        LocalDateTime time = LocalDateTime.parse(parts[2], FORMATTER); 
                        reservations.add(new Reservation(id, name, time)); 
                        if (id > maxId) { 
                            maxId = id; 
                        } 
                    } catch (NumberFormatException e) { 
                        System.err.println("Skipping invalid data file line (NumberFormatException): " +
                                line + " - " + e.getMessage()); 
                    } catch (DateTimeParseException e) { 
                        System.err.println("Skipping invalid data file line (DateTimeParseException): " +
                                line + " - " + e.getMessage()); 
                    } 
                } 
            } 
            idCounter.set(maxId); 
        } catch (IOException e) { 
            System.err.println("Error loading reservations (IOException): " + e.getMessage()); 
        } catch (Exception e) { 
            System.err.println("An unexpected error occurred while loading reservations: " + e.getMessage()); 
            e.printStackTrace(); 
        } 
    }

    public void exportToCSV(BufferedWriter writer) throws IOException {
        writer.write("ID,名前,予約日時");
        writer.newLine();
        for (Reservation res : reservations) {
            writer.write(String.format("%d,%s,%s", res.getId(), res.getName(),
                    res.getReservationTime().format(FORMATTER)));
            writer.newLine();
        }
    }
} 
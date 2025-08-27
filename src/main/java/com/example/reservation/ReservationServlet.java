package com.example.reservation; 
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
 
 
@MultipartConfig 
public class ReservationServlet extends HttpServlet { 
    private final ReservationDAO reservationDAO = new ReservationDAO(); 
 
    @Override 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action"); 
 
        if ("list".equals(action) || action == null) { 
            String searchTerm = req.getParameter("search"); 
            String sortBy = req.getParameter("sortBy"); 
            String sortOrder = req.getParameter("sortOrder"); 
            String message = req.getParameter("message");
            int page = 1; 
            int recordsPerPage = 5; 
 
            if (req.getParameter("page") != null) { 
                page = Integer.parseInt(req.getParameter("page")); 
            }

            
            
            List<Reservation> allReservations = reservationDAO.searchAndSortReservations(searchTerm, sortBy, 
sortOrder); 
 
            // ページ番号の妥当性チェック
            if (page < 1) page = 1;
            
            int totalRecords = allReservations.size();
            int noOfPages = Math.max(1, (int) Math.ceil(totalRecords * 1.0 / recordsPerPage));
            
            // ページ番号が最大ページ数を超えている場合は最大ページに調整
            if (page > noOfPages) page = noOfPages;
            
            int start = Math.max(0, (page - 1) * recordsPerPage); 
            int end = Math.min(start + recordsPerPage, totalRecords); 
            List<Reservation> reservations;
            if (start >= totalRecords || totalRecords == 0) {
                reservations = new ArrayList<>();
            } else {
                reservations = allReservations.subList(start, end);
            } 
 
 
 
            req.setAttribute("reservations", reservations); 
            req.setAttribute("noOfPages", noOfPages); 
            req.setAttribute("currentPage", page); 
            req.setAttribute("searchTerm", searchTerm); 
            req.setAttribute("sortBy", sortBy); 
            req.setAttribute("sortOrder", sortOrder);
            
            // インポート成功メッセージの処理
            if (message != null && !message.trim().isEmpty()) {
                req.setAttribute("successMessage", message);
            } 
 
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/list.jsp"); 
            rd.forward(req, resp); 
        } else if ("edit".equals(action)) { 
            int id = Integer.parseInt(req.getParameter("id")); 
            Reservation reservation = reservationDAO.getReservationById(id); 
            req.setAttribute("reservation", reservation); 
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
            rd.forward(req, resp); 
        } else if ("export_csv".equals(action)) { 
            exportCsv(req, resp); 
        } else if ("clean_up".equals(action)) { 
            reservationDAO.cleanUpPastReservations(); 
            req.setAttribute("successMessage", "過去の予約をクリーンアップしました。"); 
            resp.sendRedirect("reservation?action=list"); 
        } else { 
            resp.sendRedirect("index.jsp"); 
        } 
    } 
 
    @Override 
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
        req.setCharacterEncoding("UTF-8"); 
        String action = req.getParameter("action"); 
 
        if ("list".equals(action)) {
            // POST経由での検索・ソート処理を直接実行
            String searchTerm = req.getParameter("search");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");
            String message = req.getParameter("message");
            int page = 1;
            int recordsPerPage = 5;

            if (req.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(req.getParameter("page"));
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }


            if (page < 1) page = 1;
            
            List<Reservation> allReservations = reservationDAO.searchAndSortReservations(searchTerm, sortBy, sortOrder);
            
            int totalRecords = allReservations.size();
            int noOfPages = Math.max(1, (int) Math.ceil(totalRecords * 1.0 / recordsPerPage));
            

            if (page > noOfPages) page = noOfPages;
            
            int start = Math.max(0, (page - 1) * recordsPerPage);
            int end = Math.min(start + recordsPerPage, totalRecords);
            
            List<Reservation> reservations;
            if (start >= totalRecords || totalRecords == 0) {
                reservations = new ArrayList<>();
            } else {
                reservations = allReservations.subList(start, end);
            }

            req.setAttribute("reservations", reservations);
            req.setAttribute("noOfPages", noOfPages);
            req.setAttribute("currentPage", page);
            req.setAttribute("searchTerm", searchTerm);
            req.setAttribute("sortBy", sortBy);
            req.setAttribute("sortOrder", sortOrder);
            

            if (message != null && !message.trim().isEmpty()) {
                req.setAttribute("successMessage", message);
            }

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/list.jsp");
            rd.forward(req, resp);
            return;
        } else if ("add".equals(action)) { 
            String name = req.getParameter("name"); 
            String reservationTimeString = req.getParameter("reservation_time");
            if (name == null || name.trim().isEmpty()) { 
                req.setAttribute("errorMessage", "名前は必須です。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                rd.forward(req, resp); 
                return; 
            }
            
 
            if (reservationTimeString == null || reservationTimeString.isEmpty()) { 
                req.setAttribute("errorMessage", "希望日時は必須です。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                rd.forward(req, resp); 
                return; 
            } 
 
            try { 
                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString); 
                if (reservationTime.isBefore(LocalDateTime.now())) { 
                    req.setAttribute("errorMessage", "過去の日時は選択できません。"); 
                    RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                    rd.forward(req, resp); 
                    return; 
                } 
                if (!reservationDAO.addReservation(name, reservationTime)) { 
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。"); 
                    RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                    rd.forward(req, resp); 
                    return; 
                } 
                resp.sendRedirect("reservation?action=list"); 
            } catch (DateTimeParseException e) { 
                req.setAttribute("errorMessage", "有効な日時を入力してください。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                rd.forward(req, resp); 
            } 
        } else if ("update".equals(action)) { 
            int id = Integer.parseInt(req.getParameter("id")); 
            String name = req.getParameter("name"); 
            String reservationTimeString = req.getParameter("reservation_time"); 
 
            if (name == null || name.trim().isEmpty()) { 
                req.setAttribute("errorMessage", "名前は必須です。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
                rd.forward(req, resp); 
                return; 
            }
            

            if (reservationTimeString == null || reservationTimeString.trim().isEmpty()) { 
                req.setAttribute("errorMessage", "希望日時は必須です。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
                rd.forward(req, resp); 
                return; 
            } 
 
            try { 
                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString); 
                if (reservationTime.isBefore(LocalDateTime.now())) { 
                    req.setAttribute("errorMessage", "過去の日時は選択できません。"); 
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
                    rd.forward(req, resp); 
                    return; 
                } 
                if (!reservationDAO.updateReservation(id, name, reservationTime)) { 
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。"); 
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
                    rd.forward(req, resp); 
                    return; 
                } 
                resp.sendRedirect("reservation?action=list"); 
            } catch (DateTimeParseException e) { 
                req.setAttribute("errorMessage", "有効な日時を入力してください。"); 
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp"); 
                rd.forward(req, resp); 
            } 
        } else if ("delete".equals(action)) { 
            int id = Integer.parseInt(req.getParameter("id")); 
            reservationDAO.deleteReservation(id); 
            resp.sendRedirect("reservation?action=list"); 
        } else if ("import_csv".equals(action)) { 
            try { 
                Part filePart = req.getPart("csvFile"); 
                if (filePart != null && filePart.getSize() > 0) { 
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream(), 
"UTF-8"))) { 
                        reservationDAO.importReservations(reader); 
                        // インポート完了後、予約一覧ページにリダイレクト
                        resp.sendRedirect("reservation?action=list&message=CSV+ファイルのインポートが完了しました"); 
                        return;
                    } 
                } 
                else { 
                    req.setAttribute("errorMessage", "インポートするファイルを選択してください。"); 
                    RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                    rd.forward(req, resp);
                    return;
                }
            } catch (Exception e) { 
                req.setAttribute("errorMessage", "CSV ファイルのインポート中にエラーが発生しました: " + 
e.getMessage()); 
                e.printStackTrace(); 
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp"); 
                rd.forward(req, resp);
                return;
            } 
        } else { 
            resp.sendRedirect("index.jsp"); 
        } 
    } 
 
    private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException { 
        resp.setContentType("text/csv; charset=UTF-8"); 
        resp.setHeader("Content-Disposition", "attachment; filename=\"reservations.csv\""); 
 
        PrintWriter writer = resp.getWriter(); 
        writer.append("ID,名前,予約日時\n"); 
 
        List<Reservation> records = reservationDAO.getAllReservations(); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"); 
 
        for (Reservation record : records) { 
            writer.append(String.format("%d,%s,%s\n", 
                    record.getId(), 
                    record.getName(), 
                    record.getReservationTime() != null ? record.getReservationTime().format(formatter) : "")); 
        } 
        writer.flush(); 
    }
    
} 
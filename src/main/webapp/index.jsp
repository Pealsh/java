<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html> 
<html lang="ja"> 
<head> 
    <meta charset="UTF-8"> 
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>簡易予約システム | Modern Reservation Platform</title> 
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css"> 
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <meta name="description" content="モダンで使いやすい予約システム。直感的な操作でスムーズな予約体験を提供します。">
    <script src="${pageContext.request.contextPath}/js/modern-ui.js" defer></script>
</head> 
<body> 
    <div class="container"> 
        <h1>予約入力</h1> 
        <form action="reservation" method="post"> 
            <input type="hidden" name="action" value="add"> 
            <p class="error-message"><c:out value="${errorMessage}"/></p>
            
            <p> 
                <label for="name">名前:</label> 
                <input type="text" id="name" name="name" value="<c:out value="${param.name}"/>" required> 
            </p> 
            <p> 
                <label for="reservation_time">希望日時:</label> 
                <input type="datetime-local" id="reservation_time" name="reservation_time" value="<c:out 
value="${param.reservation_time}"/>" required> 
            </p> 
            <div class="button-group"> 
                <input type="submit" value="予約する"> 
            </div> 
        </form> 
 
        <hr> 
 
        <h2>CSV インポート</h2> 
        <form action="reservation" method="post" enctype="multipart/form-data"> 
            <input type="hidden" name="action" value="import_csv"> 
            <p> 
                <label for="csvFile">CSV ファイルを選択:</label> 
                <input type="file" id="csvFile" name="csvFile" accept=".csv" required> 
            </p> 
            <div class="button-group"> 
                <input type="submit" value="インポート"> 
            </div>
        </form> 
        <p class="success-message"><c:out value="${successMessage}"/></p> 
 
        <div class="button-group"> 
            <a href="reservation?action=list" class="button secondary">予約一覧を見る</a> 
        </div> 
    </div> 
</body> 
</html> 
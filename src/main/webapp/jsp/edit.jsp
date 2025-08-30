<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<!DOCTYPE html> 
<html lang="ja"> 
<head> 
    <meta charset="UTF-8"> 
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>予約編集 | Modern Reservation Platform</title> 
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css"> 
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <meta name="description" content="予約情報を簡単に編集・更新できるモダンなインターフェース。">
    <script src="${pageContext.request.contextPath}/js/modern-ui.js" defer></script>
</head> 
<body> 
    <div class="container"> 
        <h1>予約編集</h1> 
        <form action="reservation" method="post"> 
            <input type="hidden" name="action" value="update"> 
            <input type="hidden" name="id" value="${reservation.id}"> 
            
            <p class="error-message"><c:out value="${errorMessage}"/></p>
            
            <p> 
                <label for="name">名前:</label> 
                <input type="text" id="name" name="name" value="<c:out value="${reservation.name}"/>" required> 
            </p> 
            <p> 
                <label for="reservation_time">希望日時:</label> 
                <input type="datetime-local" id="reservation_time" name="reservation_time" value="<c:out 
value="${reservation.reservationTime}"/>" required> 
            </p> 
            <div class="button-group"> 
                <input type="submit" value="更新"> 
                <a href="reservation?action=list" class="button secondary">予約一覧に戻る</a> 
            </div> 
        </form> 
    </div> 
</body> 
</html>
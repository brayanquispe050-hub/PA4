<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - Error</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<main class="contenedor">
    <section class="tarjeta">
        <p class="eyebrow">Sistema de Pedidos</p>
        <h1>No se pudo completar la solicitud</h1>
        <div class="alerta error" role="alert">
            Ocurrió un error interno. Inténtalo nuevamente; si el problema continúa, contacta con soporte.
        </div>
        <c:url var="pedidosUrl" value="/pedidos"/>
        <a class="boton-link boton-link-secundario" href="${pedidosUrl}">Volver a pedidos</a>
    </section>
</main>
</body>
</html>
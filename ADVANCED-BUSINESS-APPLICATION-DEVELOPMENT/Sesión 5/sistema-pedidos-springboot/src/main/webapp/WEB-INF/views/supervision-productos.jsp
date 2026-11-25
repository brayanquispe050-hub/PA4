<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de productos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<main class="contenedor supervision-page">
    <%@ include file="fragments/navegacion.jspf" %>

    <header class="cabecera cabecera-supervision">
        <div>
            <p class="eyebrow">Catálogo e inventario</p>
            <h1>Supervisión de productos</h1>
            <p class="subtitulo">
                Revisa precios y disponibilidad del catálogo que utiliza el registro de pedidos.
                Los datos se consultan directamente desde Inventario.
            </p>
        </div>
    </header>

    <c:if test="${not empty errorCatalogo}">
        <div class="alerta error" role="alert">
            <c:out value="${errorCatalogo}"/>
        </div>
    </c:if>

    <section class="resumen-revisiones resumen-productos" aria-label="Resumen del catálogo">
        <article class="indicador-revision indicador-productos-total">
            <span class="indicador-etiqueta">Productos en catálogo</span>
            <strong><c:out value="${fn:length(productos)}"/></strong>
            <span class="indicador-nota">consultados desde Inventario</span>
        </article>
        <article class="indicador-revision indicador-completados">
            <span class="indicador-etiqueta">Disponibles</span>
            <strong><c:out value="${productosDisponibles}"/></strong>
            <span class="indicador-nota">con stock para venta</span>
        </article>
        <article class="indicador-revision indicador-rechazados">
            <span class="indicador-etiqueta">Agotados</span>
            <strong><c:out value="${productosAgotados}"/></strong>
            <span class="indicador-nota">sin unidades disponibles</span>
        </article>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla">
            <h2>Catálogo supervisado</h2>
            <p>La información de precio y stock corresponde a la última consulta del servicio de inventario.</p>
        </div>
        <div class="tabla-scroll">
            <table class="tabla-productos">
                <thead>
                <tr>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Precio unitario</th>
                    <th>Stock actual</th>
                    <th>Disponibilidad</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="producto" items="${productos}">
                    <tr>
                        <td><span class="codigo-producto"><c:out value="${producto.codigo}"/></span></td>
                        <td class="producto-nombre"><c:out value="${producto.nombre}"/></td>
                        <td>
                            S/ <fmt:formatNumber value="${producto.precio}"
                                                 minFractionDigits="2" maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${producto.stock}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${producto.stock gt 0}">
                                    <span class="estado-revision estado-aceptado">Disponible</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="estado-revision estado-pendiente">Agotado</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty productos}">
                    <tr>
                        <td colspan="5" class="sin-datos">
                            <c:choose>
                                <c:when test="${not empty errorCatalogo}">
                                    No se pudo consultar el catálogo. Vuelve a intentarlo cuando Inventario esté disponible.
                                </c:when>
                                <c:otherwise>No hay productos registrados en el catálogo.</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>

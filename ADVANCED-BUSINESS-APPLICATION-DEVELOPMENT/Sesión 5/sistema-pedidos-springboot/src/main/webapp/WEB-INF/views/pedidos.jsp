<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <%@ include file="fragments/navegacion.jspf" %>

    <header class="hero-pagina">
        <div>
            <p class="eyebrow">Gestión operativa</p>
            <h1>Pedidos</h1>
            <p class="subtitulo">
                Registra ventas y consulta el estado de supervisión de cada pedido.
            </p>
        </div>
        <span class="flujo">Catálogo e inventario en tiempo real · Pedidos con trazabilidad</span>
    </header>

    <section class="tarjeta">
        <c:if test="${not empty error}">
            <div class="alerta error">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <c:if test="${not empty param.creado}">
            <div class="alerta exito">
                Pedido #<c:out value="${param.creado}"/> registrado correctamente.
            </div>
        </c:if>

        <h2>Registrar pedido</h2>

        <c:url var="pedidosUrl" value="/pedidos"/>

        <form method="post" action="${pedidosUrl}" class="form-grid">
            <!-- Token CSRF -->
            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}">

            <label>
                Cliente
                <input
                        name="cliente"
                        required
                        maxlength="120"
                        placeholder="Ej. Ana Torres"
                        value="${fn:escapeXml(clienteIngresado)}">
            </label>

            <label>
                Producto
                <select name="productoCodigo" required>
                    <option value="">Seleccione un producto</option>
                    <c:forEach var="producto" items="${productos}">
                        <option
                                value="${fn:escapeXml(producto.codigo)}"
                                ${producto.stock le 0 ? 'disabled' : ''}
                                ${producto.codigo eq productoCodigoIngresado ? 'selected' : ''}>
                            <c:out value="${producto.codigo}"/> -
                            <c:out value="${producto.nombre}"/> - S/
                            <fmt:formatNumber
                                    value="${producto.precio}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                            - stock: <c:out value="${producto.stock}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>

            <label>
                Cantidad
                <input
                        name="cantidad"
                        type="number"
                        min="1"
                        value="${empty cantidadIngresada ? 1 : fn:escapeXml(cantidadIngresada)}"
                        required>
            </label>

            <button type="submit">Registrar</button>
        </form>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla titulo-tabla-acciones">
            <div>
                <h2>Pedidos registrados</h2>
                <p>
                    Cada pedido conserva un snapshot del código, nombre y precio vigente al momento de la compra.
                </p>
            </div>

            <div class="acciones-navegacion">
                <c:url var="supervisionPedidosUrl" value="/supervision/pedidos"/>
                <c:choose>
                    <c:when test="${puedeSupervisar}">
                    <a href="${supervisionPedidosUrl}" class="boton-link boton-link-secundario">
                        Ver supervisión de pedidos
                    </a>
                    </c:when>
                    <c:otherwise>
                        <button type="button"
                                class="boton-link boton-link-secundario boton-link-restringido"
                                disabled
                                aria-label="Ver supervisión de pedidos. Disponible solo para el rol OPERADOR.">
                            Ver supervisión de pedidos (solo OPERADOR)
                        </button>
                    </c:otherwise>
                </c:choose>
                <c:if test="${puedeSupervisar}">
                    <c:url var="supervisionProductosUrl" value="/supervision/productos"/>
                    <a href="${supervisionProductosUrl}" class="boton-link boton-link-secundario">
                        Ver supervisión de productos
                    </a>
                </c:if>
                <c:url var="reportePedidosUrl" value="/reportes/pedidos"/>
                <a href="${reportePedidosUrl}" class="boton-link boton-link-primario">
                    <span aria-hidden="true">▦</span>
                    Ver reporte de pedidos
                </a>
            </div>
        </div>

        <div class="tabla-scroll">
            <table class="tabla-pedidos">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Precio unitario</th>
                    <th>Cantidad</th>
                    <th>Total</th>
                    <th>Fecha</th>
                    <th>Revisión</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.productoCodigo}"/></td>
                        <td><c:out value="${pedido.productoNombre}"/></td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.precioUnitario}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${pedido.cantidad}"/></td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.total}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${pedido.fecha}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${pedido.estadoRevision eq 'ACEPTADO'}">
                                    <span class="estado-revision estado-aceptado">Aceptado</span>
                                </c:when>
                                <c:when test="${pedido.estadoRevision eq 'RECHAZADO'}">
                                    <span class="estado-revision estado-rechazado">Rechazado</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="estado-revision estado-pendiente">Pendiente</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty pedidos}">
                    <tr>
                        <td colspan="9" class="sin-datos">Aún no hay pedidos.</td>
                    </tr>
                </c:if>

                </tbody>
            </table>
        </div>
    </section>
</div>
</body>
</html>

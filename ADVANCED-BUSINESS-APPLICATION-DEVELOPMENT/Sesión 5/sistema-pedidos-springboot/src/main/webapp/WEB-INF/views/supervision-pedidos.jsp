<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de pedidos</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<main class="contenedor supervision-page">
    <%@ include file="fragments/navegacion.jspf" %>

    <header class="cabecera cabecera-supervision">
        <div>
            <p class="eyebrow">Centro de control · PA4</p>
            <h1>Supervisión de pedidos</h1>
            <p class="subtitulo">
                Registra el resultado de cada verificación y confirma la supervisión del pedido.
            </p>
        </div>
        <c:url var="pedidosUrl" value="/pedidos"/>
        <a class="supervision-back" href="${pedidosUrl}">Volver a pedidos</a>
    </header>

    <c:if test="${not empty mensaje}">
        <div class="alerta exito" role="status"><c:out value="${mensaje}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alerta error" role="alert"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty errorReporte}">
        <div class="alerta error" role="alert"><c:out value="${errorReporte}"/></div>
    </c:if>

    <c:if test="${not empty estadisticasRevisiones}">
        <section class="resumen-revisiones" aria-label="Resumen de revisiones">
            <article class="indicador-revision">
                <span class="indicador-etiqueta">Pedidos totales</span>
                <strong><c:out value="${estadisticasRevisiones.totalPedidos}"/></strong>
                <span class="indicador-nota">registrados</span>
            </article>
            <article class="indicador-revision indicador-pendientes">
                <span class="indicador-etiqueta">Pendientes</span>
                <strong><c:out value="${estadisticasRevisiones.totalPendientes}"/></strong>
                <span class="indicador-nota">requieren revisión</span>
            </article>
            <article class="indicador-revision indicador-completados">
                <span class="indicador-etiqueta">Aceptados</span>
                <strong><c:out value="${totalAceptados}"/></strong>
                <span class="indicador-nota">pedidos aprobados</span>
            </article>
            <article class="indicador-revision indicador-rechazados">
                <span class="indicador-etiqueta">Rechazados</span>
                <strong><c:out value="${totalRechazados}"/></strong>
                <span class="indicador-nota">requieren seguimiento</span>
            </article>
        </section>
    </c:if>

    <section class="seccion-supervision" aria-labelledby="pendientes-titulo">
        <div class="titulo-seccion">
            <div>
                <p class="eyebrow">Acción requerida</p>
                <h2 id="pendientes-titulo">Pendientes de supervisión</h2>
                <p>Escribe una observación obligatoria y confirma cada revisión.</p>
            </div>
            <span class="contador-supervision">
                <c:out value="${fn:length(pedidosPendientes)}"/> pendientes
            </span>
        </div>

        <div class="lista-supervision">
            <c:forEach var="pedido" items="${pedidosPendientes}">
                <article class="pedido-supervision">
                    <div class="pedido-supervision-cabecera">
                        <div class="pedido-identidad">
                            <span class="pedido-id">Pedido #<c:out value="${pedido.pedidoId}"/></span>
                            <span class="estado-revision estado-pendiente">Pendiente</span>
                        </div>
                        <strong class="pedido-total">
                            S/ <fmt:formatNumber value="${pedido.total}"
                                                 minFractionDigits="2" maxFractionDigits="2"/>
                        </strong>
                    </div>

                    <dl class="datos-supervision">
                        <div>
                            <dt>Cliente</dt>
                            <dd><c:out value="${pedido.cliente}"/></dd>
                        </div>
                        <div>
                            <dt>Producto</dt>
                            <dd>
                                <span class="codigo-producto"><c:out value="${pedido.productoCodigo}"/></span>
                                <c:out value="${pedido.productoNombre}"/>
                            </dd>
                        </div>
                    </dl>

                    <c:url var="revisarUrl" value="/supervision/pedidos/${pedido.pedidoId}/revisar"/>
                    <form class="form-revision" method="post" action="${revisarUrl}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <div class="form-revision-titulo">
                            <label for="observacion-${pedido.pedidoId}">Observación de supervisión</label>
                            <span>Obligatoria</span>
                        </div>
                        <textarea id="observacion-${pedido.pedidoId}" name="observacion"
                                  maxlength="500" minlength="1" rows="3"
                                  aria-describedby="ayuda-observacion-${pedido.pedidoId}"
                                  placeholder="Describe qué verificaste y el resultado..."
                                  required></textarea>
                        <div class="form-revision-pie">
                            <small id="ayuda-observacion-${pedido.pedidoId}">
                                Máximo 500 caracteres. Se guardará en la trazabilidad del pedido.
                            </small>
                            <div class="acciones-decision">
                                <button type="submit" name="resultado" value="ACEPTADO"
                                        class="boton-aceptar"
                                        onclick="return confirm('¿Confirmas que deseas aceptar este pedido?')">
                                    Aceptar pedido
                                </button>
                                <button type="submit" name="resultado" value="RECHAZADO"
                                        class="boton-rechazar"
                                        onclick="return confirm('¿Confirmas que deseas rechazar este pedido?')">
                                    Rechazar pedido
                                </button>
                            </div>
                        </div>
                    </form>
                </article>
            </c:forEach>

            <c:if test="${empty pedidosPendientes}">
                <div class="vacio-supervision">
                    <span class="vacio-icono" aria-hidden="true">✓</span>
                    <h3>
                        <c:choose>
                            <c:when test="${not empty errorReporte}">No se pudo cargar la cola</c:when>
                            <c:otherwise>Todo está al día</c:otherwise>
                        </c:choose>
                    </h3>
                    <p>
                        <c:choose>
                            <c:when test="${not empty errorReporte}">
                                Verifica la disponibilidad del servicio de reportes e inténtalo nuevamente.
                            </c:when>
                            <c:otherwise>No hay pedidos pendientes de supervisión en este momento.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:if>
        </div>
    </section>

    <section class="seccion-supervision" aria-labelledby="supervisados-titulo">
        <div class="titulo-seccion">
            <div>
                <p class="eyebrow">Registro de actividad</p>
                <h2 id="supervisados-titulo">Supervisiones completadas</h2>
                <p>Consulta las observaciones y la fecha de cada confirmación.</p>
            </div>
            <span class="contador-supervision contador-completados">
                <c:out value="${fn:length(pedidosRevisados)}"/> completados
            </span>
        </div>

        <div class="lista-supervision lista-supervisados">
            <c:forEach var="pedido" items="${pedidosRevisados}">
                <article class="pedido-supervision pedido-supervisado">
                    <div class="pedido-supervision-cabecera">
                        <div class="pedido-identidad">
                            <span class="pedido-id">Pedido #<c:out value="${pedido.pedidoId}"/></span>
                            <c:choose>
                                <c:when test="${pedido.resultado eq 'ACEPTADO'}">
                                    <span class="estado-revision estado-aceptado">Aceptado</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="estado-revision estado-rechazado">Rechazado</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <strong class="pedido-total">
                            S/ <fmt:formatNumber value="${pedido.total}"
                                                 minFractionDigits="2" maxFractionDigits="2"/>
                        </strong>
                    </div>

                    <dl class="datos-supervision">
                        <div>
                            <dt>Cliente</dt>
                            <dd><c:out value="${pedido.cliente}"/></dd>
                        </div>
                        <div>
                            <dt>Producto</dt>
                            <dd>
                                <span class="codigo-producto"><c:out value="${pedido.productoCodigo}"/></span>
                                <c:out value="${pedido.productoNombre}"/>
                            </dd>
                        </div>
                    </dl>

                    <div class="observacion-registrada">
                        <span class="observacion-etiqueta">Observación registrada</span>
                        <p><c:out value="${empty pedido.observacion ? 'Sin detalle registrado' : pedido.observacion}"/></p>
                    </div>
                    <div class="datos-confirmacion">
                        <span>Supervisó <strong><c:out value="${empty pedido.revisadoPor ? '—' : pedido.revisadoPor}"/></strong></span>
                        <span><c:out value="${empty pedido.fechaRevision ? 'Fecha no disponible' : pedido.fechaRevision}"/></span>
                    </div>
                </article>
            </c:forEach>

            <c:if test="${empty pedidosRevisados}">
                <div class="vacio-supervision vacio-secundario">
                    <h3>Aún no hay supervisiones completadas</h3>
                    <p>Cuando confirmes una revisión, su observación aparecerá aquí.</p>
                </div>
            </c:if>
        </div>
    </section>
</main>
</body>
</html>

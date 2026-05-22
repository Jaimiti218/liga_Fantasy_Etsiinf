let ligaId    = null;
let miEquipoId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const partes = window.location.pathname.split('/');
    ligaId = parseInt(partes[partes.length - 1]);
    if (!ligaId) { window.location.href = '/fantasy/mis-ligas'; return; }

    await cargarDatosEquipo();
    await cargarNoticias();
});

function irAClasificacion() { window.location.href = `/fantasy/liga/${ligaId}`; }
function irAMercado()       { window.location.href = `/fantasy/mercado/${ligaId}`; }
function irAPlantilla()     { window.location.href = `/fantasy/plantilla/${miEquipoId}?liga=${ligaId}`; }

async function cargarDatosEquipo() {
    try {
        const res = await fetch(`/equipos-fantasy/liga/${ligaId}/mi-equipo`, {
            credentials: 'include'
        });
        if (!res.ok) return;
        const datos = await res.json();
        miEquipoId = datos.id;
    } catch (e) {}
}

async function cargarNoticias() {
    const contenedor = document.getElementById('lista-noticias');
    try {
        const res = await fetch(`/noticias/liga/${ligaId}`, {
            credentials: 'include'
        });
        if (!res.ok) {
            contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar las noticias.</div>';
            return;
        }

        const noticias = await res.json();

        if (noticias.length === 0) {
            contenedor.innerHTML = '<div class="cargando-ligas">No hay noticias todavía.</div>';
            return;
        }

        contenedor.innerHTML = noticias.map(n => {
            const claseColor = n.titulo.replace(/\s+/g, '-').toUpperCase();
            const clasePrivada = n.esPrivada ? 'privada' : '';
            const badgePrivada = n.esPrivada
                ? '<span class="noticia-privada-badge">🔒 Solo tú</span>'
                : '';
            const clasesTitulo = `titulo-${claseColor}`;
            const fecha = new Date(n.fecha[0], n.fecha[1] - 1, n.fecha[2]);
            const fechaStr = new Date(n.fecha + 'T00:00:00').toLocaleDateString('es-ES', {
                day: '2-digit', month: 'long', year: 'numeric'
            });
            const horaStr = n.hora ? n.hora.substring(0, 5) : '';

            return `
            <div class="noticia-card ${claseColor} ${clasePrivada}">
                <div class="noticia-header">
                    <span class="noticia-titulo ${clasesTitulo}">
                        ${n.titulo}${badgePrivada}
                    </span>
                    <span class="noticia-hora">${horaStr}</span>
                </div>
                <div class="noticia-texto">${n.noticia}</div>
                <div class="noticia-fecha">${fechaStr}</div>
            </div>`;
        }).join('');

    } catch (e) {
        contenedor.innerHTML = '<div class="cargando-ligas">Error al cargar.</div>';
    }
}
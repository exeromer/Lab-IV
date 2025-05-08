export interface Instrumento { // MANTENER - Esta es tu entidad completa de Instrumento, muy útil.
    id?: number;
    instrumento: string;
    imagen: string;
    precio: number;
    costoEnvio: string;
    cantidadVendida: number;
    marca: string;
    modelo: string;
    descripcion: string;
    categoria: Categoria;
}
export interface Categoria { // MANTENER - Necesaria para Instrumento.
    id: number;
    denominacion: string;
}
export interface CardProps { // MANTENER - No relacionada directamente con el refactor del carrito.
    instrumento: Instrumento;
}

export interface ModalProps { // MANTENER - No relacionada directamente con el refactor del carrito.
    show: boolean;
    handleClose: () => void;
    instrumento: Instrumento;
}

export interface CartContextType {
    itemsDelCarrito: ItemCarritoNuevo[]; // <--- CAMBIO AQUÍ
    agregarAlCarrito: (instrumento: Instrumento) => void; // Se mantiene igual, recibe el Instrumento completo
    limpiarCarrito: () => void;
    modificarCantidad: (instrumentoId: number, cantidad: number) => void; // <--- CAMBIO AQUÍ (id -> instrumentoId)
    eliminarItem: (instrumentoId: number) => void; // <--- CAMBIO AQUÍ (id -> instrumentoId)
}

export interface ItemCarritoNuevo {
    instrumentoId: number;       // Corresponde al id del Instrumento
    nombreInstrumento: string;   // Corresponde al campo 'instrumento' del Instrumento (el nombre)
    cantidad: number;
    precioUnitario: number;    // Será el 'precio' del Instrumento al momento de añadirlo
    imagenInstrumento: string;   // Corresponde a la 'imagen' del Instrumento (ya que la usabas)
}

export interface CarritoAsideProps {
    visible: boolean;
    onClose: () => void;
    // Las siguientes dos líneas se eliminan si no se pasan como props directas:
    modificarCantidad: (instrumentoId: number, cantidad: number) => void;
    eliminarItem: (instrumentoId: number) => void;
}

export interface Pedido { // MANTENER - Útil para tipar la respuesta del backend al guardar.
    id: number;
    fecha: string;
    total: number;
    detalles: PedidoDetalle[];
}

export interface PedidoDetalle { // MANTENER - Útil para tipar la respuesta del backend.
    id: number;
    cantidad: number;
    precioUnitario: number;
    instrumento: Instrumento; // El backend devuelve el Instrumento completo aquí.
}
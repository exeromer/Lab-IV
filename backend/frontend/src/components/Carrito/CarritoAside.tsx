import { useCart } from '../../context/CartContext'; // Asumiendo que CartContext está en esta ruta
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMinus, faPlus, faTrashCan } from '@fortawesome/free-solid-svg-icons';
import './CarritoAside.sass'; // Asegúrate que la ruta al SASS sea correcta
import Titulo from '../Titulo/Titulo'; // Asegúrate que la ruta al componente Titulo sea correcta
import { useState, useEffect } from 'react'; // useEffect añadido si quieres limpiar mensajes al desmontar
import { CarritoAsideProps } from '../../types/types'; // Asegúrate que esta interfaz esté actualizada

export const CarritoAside: React.FC<CarritoAsideProps> = ({ visible, onClose }) => {
  // Paso 1: Actualizar la desestructuración de useCart()
  const { itemsDelCarrito, limpiarCarrito, modificarCantidad, eliminarItem } = useCart();
  const [mensaje, setMensaje] = useState<string | null>(null);

  // Opcional: Limpiar mensaje cuando el carrito se cierra o desmonta
  useEffect(() => {
    if (!visible) {
      setMensaje(null);
    }
  }, [visible]);

  // Paso 2: Actualizar handleGuardarPedido
  const handleGuardarPedido = async () => {
    try {
      const detallesParaEnviar = itemsDelCarrito.map(item => ({
        instrumentoId: item.instrumentoId,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario
      }));

      if (detallesParaEnviar.length === 0) {
        setMensaje('⚠️ El carrito está vacío.');
        setTimeout(() => setMensaje(null), 3000);
        return;
      }

      const response = await fetch('http://localhost:8080/api/pedido', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ detalles: detallesParaEnviar }), // El backend espera un objeto { detalles: [...] }
      });

      const data = await response.json(); // Asumimos que el backend devuelve el Pedido creado o un error
      
      if (response.ok) {
        // Asumiendo que 'data' es el Pedido guardado y tiene un 'id'
        setMensaje(`✅ Pedido guardado con éxito. ID: ${data.id || ''}`);
        limpiarCarrito();
        setTimeout(() => {
          setMensaje(null);
          onClose(); // Cierra el carrito
        }, 3000); // Mensaje de éxito un poco más largo
      } else {
        // Mejor manejo de errores del backend
        const errorMsg = data.message || (data.errors && data.errors[0]?.defaultMessage) || data.error || 'Error al guardar el pedido';
        throw new Error(errorMsg);
      }
    } catch (error) {
      console.error('Error al guardar pedido:', error);
      setMensaje(error instanceof Error ? `❌ ${error.message}` : '❌ Error desconocido al procesar el pedido');
      setTimeout(() => setMensaje(null), 5000); // Mantener mensaje de error más tiempo
    }
  };

  if (!visible) return null;

  // Paso 3: Actualizar el cálculo del total
  const total = itemsDelCarrito.reduce((acc, item) => acc + item.precioUnitario * item.cantidad, 0);

  return (
    <div className="carrito-overlay" onClick={onClose}>
      <aside className="carrito-aside" onClick={e => e.stopPropagation()}>
        <Titulo texto='Carrito' />
        
        {mensaje && <div className={`feedback-mensaje ${mensaje.startsWith('✅') ? 'exito' : 'error'}`}>{mensaje}</div>}

        {/* Paso 4: Actualizar el renderizado de la lista de ítems */}
        {itemsDelCarrito.length === 0 ? (
          <p style={{ textAlign: 'center', fontSize: '20px', marginTop: '20px' }}>El carrito está vacío</p>
        ) : (
          <ul style={{ padding: 0, margin: 0, listStyleType: 'none' }}>
            {itemsDelCarrito.map(item => ( // 'item' es ahora un ItemCarritoNuevo
              <li key={item.instrumentoId} className='carrito-item'> {/* key ahora es item.instrumentoId */}
                <div className='item-imagen-container'>
                  {/* Asumiendo que tienes item.imagenInstrumento y quieres mostrarla */}
                  <img src={item.imagenInstrumento} alt={item.nombreInstrumento} className='item-imagen-carrito' />
                </div>
                <div className='contenedor-texto'>
                  <span>{item.nombreInstrumento}</span> {/* Usa la propiedad nombreInstrumento */}
                  <span>${item.precioUnitario.toFixed(2)}</span> {/* Usa la propiedad precioUnitario */}
                </div>

                <div className='botones-contenedor'>
                  <div>
                    <button 
                      className='cantidad' 
                      onClick={() => modificarCantidad(item.instrumentoId, item.cantidad - 1)}
                      disabled={item.cantidad <= 0} // Deshabilitar si la cantidad es 0 (aunque se filtra)
                    >
                      <FontAwesomeIcon icon={faMinus} />
                    </button>
                    <span style={{ padding: '0 10px', minWidth: '20px', textAlign: 'center', display: 'inline-block' }}>{item.cantidad}</span>
                    <button className='cantidad' onClick={() => modificarCantidad(item.instrumentoId, item.cantidad + 1)}>
                      <FontAwesomeIcon icon={faPlus} />
                    </button>
                  </div>
                  <button className='eliminar' onClick={() => eliminarItem(item.instrumentoId)}> {/* Pasar item.instrumentoId */}
                    <FontAwesomeIcon icon={faTrashCan} />
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
        
        {itemsDelCarrito.length > 0 && ( // Solo mostrar total y botones si hay items
          <div className='botones-carrito'>
            <p style={{ margin: '8px 0 16px 0', textAlign: 'center', fontSize: '1.3em', fontWeight: 'bold' }}>
              Total: ${total.toFixed(2)}
            </p>
            <button className='vaciar-carrito' onClick={limpiarCarrito}>Vaciar Carrito</button>
            <button className='guardar-pedido' onClick={handleGuardarPedido}>Guardar Pedido</button>
          </div>
        )}
        <button className='cerrar-carrito' onClick={onClose} style={{marginTop: itemsDelCarrito.length > 0 ? '10px' : '20px'}}>Cerrar</button>
      </aside>
    </div>
  );
};

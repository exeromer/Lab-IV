import { useCart } from '../../context/CartContext'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faMinus, faPlus, faTrashCan } from '@fortawesome/free-solid-svg-icons'
import './CarritoAside.sass'
import Titulo from '../Titulo/Titulo'
import { useState } from 'react'
import { CarritoAsideProps, PedidoRequest } from '../../types/types'

//Declaracion MercadoPago 
declare global {
  interface Window {
    MercadoPago: any;
  }
}

export const CarritoAside: React.FC<CarritoAsideProps> = ({ visible, onClose }) => {
  const { carrito, limpiarCarrito, modificarCantidad, eliminarItem } = useCart()
  const [mensaje, setMensaje] = useState<string | null>(null); //Estado para mensajes
  const [isLoading, setIsLoading] = useState(false);

  const MERCADOPAGO_PUBLIC_KEY = "APP_USR-9a1a1cb2-bc56-4419-a061-7e7c20ba1127"; // Reemplaza con clave de publicación de MercadoPago


  const handleGuardarPedido = async () => {
    if (carrito.length === 0) {
      setMensaje("El carrito está vacío.");
      return;
    }
    setIsLoading(true);
    setMensaje("Procesando pedido...");

    try {
      // Calcular el total del pedido
      const calcularTotal = () => {
        return carrito.reduce((total, item) => {
          return total + (item.precio * item.cantidad);
        }, 0);
      };

      const pedido: PedidoRequest = {
        fecha: new Date().toISOString(),
        total: calcularTotal(),
        detalles: carrito.map(item => ({
          instrumentoId: item.id,
          cantidad: item.cantidad,
        })),
      }

      const response = await fetch('http://localhost:8080/api/pedidos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(pedido), // Enviar el objeto 'pedido' completo
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || 'Error al guardar el pedido');
      }
      const orderId = data.id; // Asumiendo que el ID viene en la raíz del DTO de respuesta
      setMensaje(`Pedido Nro. ${orderId} guardado. Preparando pago...`);
      
      // Realizar el pago con MercadoPago
      const responsePreferencia = await fetch(`http://localhost:8080/api/pedidos/${orderId}/preferencia`, {
        method: 'POST', // Confirmado por tu PedidoController.java
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const dataPreferencia = await responsePreferencia.json();

      if (!responsePreferencia.ok) {
        throw new Error(dataPreferencia.error || 'Error al crear la preferencia de pago');
      }

      // El backend devuelve preferenceId y initPoint
      const preferenceId = dataPreferencia.preferenceId;
      const initPointUrl = dataPreferencia.initPoint; // <-- URL de redirección directa

      if (!preferenceId && !initPointUrl) { // Verificamos si alguno de los dos existe
        throw new Error('No se recibió el ID de preferencia o el init_point de Mercado Pago.');
      }

      // Redirigir al usuario a la URL de redirección directa de Mercado Pago
      if (initPointUrl) {
        window.location.href = initPointUrl;
      }
      else if (preferenceId) { // Si no hay initPoint pero sí preferenceId, y el SDK está cargado
         if (window.MercadoPago) {
            const mp = new window.MercadoPago(MERCADOPAGO_PUBLIC_KEY, {
             locale: 'es-AR' // Ajusta la localización si es necesario
           });
           mp.checkout({
             preference: {
               id: preferenceId
             },
             autoOpen: true,
           });
         } else {
            console.error("El SDK de Mercado Pago no está cargado y no se pudo usar el preferenceId.");
            throw new Error("Error al iniciar el pago: SDK de Mercado Pago no disponible para usar preferenceId.");
         }
       }
      else {
        console.error("El SDK de Mercado Pago no está cargado o no se recibió información para la redirección.");
        throw new Error("Error al iniciar el pago: No se pudo redirigir a Mercado Pago.");
      }

    } catch (error) {
      console.error('Error en el proceso:', error);
      setMensaje(error instanceof Error ? `❌ ${error.message}` : '❌ Error desconocido durante el proceso');
      setIsLoading(false);
    }
  };

  if (!visible) return null;

  const total = carrito.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  return (
    <div className="carrito-overlay" onClick={isLoading ? undefined : onClose}> {/* Evita cerrar el overlay si está cargando */}
      <aside className="carrito-aside" onClick={e => e.stopPropagation()}>
        <Titulo texto='Carrito' />

        {/* Mensaje de feedback */}
        {/* Aplicamos una clase 'error' o 'success' para dar estilo diferente si quieres */}
        {mensaje && (
          <div className={`feedback-mensaje ${mensaje.startsWith("❌") ? 'error' : 'success'}`}>
            {mensaje}
          </div>
        )}

        {carrito.length === 0 && !isLoading ? ( // Solo muestra "El carrito está vacío" si no está cargando
          <p style={{ textAlign: 'center', fontSize: '20px' }}>El carrito está vacío</p>
        ) : (
          // Solo muestra la lista de ítems si el carrito NO está vacío, incluso si está cargando
          // ya que el mensaje de "Procesando..." estará visible.
          carrito.length > 0 && (
            <ul style={{ padding: 0, margin: 0 }}>
              {carrito.map(item => (
                <li key={item.id} className='carrito-item'>
                  <div className='contenedor-texto'>
                    <span>{item.instrumento}</span>
                    <span>${item.precio.toFixed(2)}</span>
                  </div>

                  <div className='botones-contenedor'>
                    <div>
                      <button
                        className='cantidad'
                        onClick={() => modificarCantidad(item.id, item.cantidad - 1)}
                        disabled={isLoading} // Deshabilitado si está cargando
                      >
                        <FontAwesomeIcon icon={faMinus} />
                      </button>
                      <span style={{ padding: '0 10px' }}>{item.cantidad}</span>
                      <button
                        className='cantidad'
                        onClick={() => modificarCantidad(item.id, item.cantidad + 1)}
                        disabled={isLoading} // Deshabilitado si está cargando
                      >
                        <FontAwesomeIcon icon={faPlus} />
                      </button>
                    </div>
                    <button
                      className='eliminar'
                      onClick={() => eliminarItem(item.id)}
                      disabled={isLoading} // Deshabilitado si está cargando
                    >
                      <FontAwesomeIcon icon={faTrashCan} />
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )
        )}

        <div className='botones-carrito'>
          <p style={{ margin: '8px', textAlign: 'center', fontSize: '1.2em', fontWeight: '600' }}>
            Total: ${total.toFixed(2)}
          </p>
          <button
            className='vaciar-carrito'
            onClick={limpiarCarrito}
            disabled={isLoading || carrito.length === 0} // Deshabilitado si carga o si no hay ítems
          >
            Vaciar Carrito
          </button>
          <button
            className='guardar-pedido' // Puedes renombrar la clase CSS si quieres (ej: 'btn-pagar-mp')
            onClick={handleGuardarPedido} // Llama a la nueva función combinada
            disabled={isLoading || carrito.length === 0} // Deshabilitado si carga o si no hay ítems
          >
            {isLoading ? "Procesando..." : "Pagar con Mercado Pago"} {/* Texto dinámico */}
          </button>
          <button
            className='cerrar-carrito'
            onClick={onClose}
            disabled={isLoading} // Deshabilitado si está cargando
          >
            Cerrar
          </button>
        </div>
      </aside>
    </div>
  );
}

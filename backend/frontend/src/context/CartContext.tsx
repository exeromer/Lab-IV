import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { Instrumento, ItemCarritoNuevo, CartContextType } from "../types/types";

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [items, setItems] = useState<ItemCarritoNuevo[]>(() => {
    const carritoGuardado = localStorage.getItem("carrito"); 

    if (carritoGuardado) { 
      try {
        const parsedItems = JSON.parse(carritoGuardado); 
        if (Array.isArray(parsedItems) &&
            (parsedItems.length === 0 || 
             (parsedItems[0] && 
              typeof parsedItems[0].instrumentoId === 'number' && 
              typeof parsedItems[0].nombreInstrumento === 'string'))) { 
          return parsedItems as ItemCarritoNuevo[]; 
        } else {
          console.warn("La estructura del carrito en localStorage no coincide con la esperada. Se iniciará un carrito vacío.");
          return []; 
        }
      } catch (error) {
        console.error("Error al parsear el carrito desde localStorage. Se iniciará un carrito vacío.", error);
        return [];
      }
    }
    return [];
  });

  useEffect(() => {
    localStorage.setItem("carrito", JSON.stringify(items)); 
  }, [items]); 

  const agregarAlCarrito = (instrumento: Instrumento) => {
    const instrumentoId = instrumento.id;

    if (typeof instrumentoId !== 'number') { 
      console.error("Instrumento sin ID numérico válido no puede ser añadido:", instrumento);
      return; 
    }

    setItems((prevItems) => {
      const existe = prevItems.find((item) => item.instrumentoId === instrumentoId);
      if (existe) {
        return prevItems.map((item) =>
          item.instrumentoId === instrumentoId
            ? { ...item, cantidad: item.cantidad + 1 }
            : item
        );
      } else {
        const nuevoItem: ItemCarritoNuevo = {
          instrumentoId: instrumentoId,
          nombreInstrumento: instrumento.instrumento, 
          precioUnitario: instrumento.precio,       
          cantidad: 1,
          imagenInstrumento: instrumento.imagen     
        };
        return [...prevItems, nuevoItem];
      }
    });
  };

  const modificarCantidad = (instrumentoId: number, cantidad: number) => { 
    setItems((prevItems) => 
      prevItems
        .map((item) =>
          item.instrumentoId === instrumentoId 
            ? { ...item, cantidad: cantidad } 
            : item
        )
        .filter((item) => item.cantidad > 0) 
    );
  };

  const eliminarItem = (instrumentoId: number) => { 
    setItems((prevItems) => prevItems.filter((item) => item.instrumentoId !== instrumentoId)); 
  };

  const limpiarCarrito = () => setItems([]); 

  return (
    <CartContext.Provider
      value={{
        itemsDelCarrito: items, 
        agregarAlCarrito,
        modificarCantidad,
        eliminarItem,
        limpiarCarrito,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) throw new Error("useCart debe usarse dentro de CartProvider");
  return context;
};

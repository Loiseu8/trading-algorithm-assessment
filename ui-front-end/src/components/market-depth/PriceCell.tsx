import { useRef } from "react";

export interface PriceCellProps {
    price: number;
    type: "bid" | "ask";
}

export const PriceCell = ({ price, type }: PriceCellProps) => {
    const lastPriceRef = useRef<number | null>(null);

    // Determine if the price has moved up or down
    const priceDirection =
        lastPriceRef.current !== null
            ? price > lastPriceRef.current
                ? "up"
                : price < lastPriceRef.current
                    ? "down"
                    : ""
            : "";

    // Update the reference for the next render
    lastPriceRef.current = price;

    return (
        <td className={`price-cell-${type}`}>
            {/* For bid, show the arrow before the price */}
            {type === "bid" && priceDirection === "up" && (
                <span className="price-arrow">&#11014;</span> /* Up arrow */
            )}
            {type === "bid" && priceDirection === "down" && (
                <span className="price-arrow">&#11015;</span> /* Down arrow */
            )}

            {price}

            {/* For ask, show the arrow after the price */}
            {type === "ask" && priceDirection === "up" && (
                <span className="price-arrow">&#11014;</span> /* Up arrow */
            )}
            {type === "ask" && priceDirection === "down" && (
                <span className="price-arrow">&#11015;</span> /* Down arrow */
            )}
        </td>
    );
};

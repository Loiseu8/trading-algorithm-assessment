import {MarketDepthRow} from "./useMarketDepthData.ts";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    console.log({ props });
    return <table></table>;
};
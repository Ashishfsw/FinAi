"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { addOrUpdateHolding } from "@/lib/api";
import { AssetHolding, AssetType } from "@/lib/types";
import { Loader2, PlusCircle } from "lucide-react";

const ASSET_TYPES: AssetType[] = ["STOCK", "BOND", "ETF", "MUTUAL_FUND", "CRYPTO", "COMMODITY", "CASH"];

interface AddHoldingCardProps {
  userId: number;
  onAdded?: (holding: AssetHolding) => void;
}

export function AddHoldingCard({ userId, onAdded }: AddHoldingCardProps) {
  const [form, setForm] = useState({
    symbol: "",
    assetName: "",
    assetType: "STOCK" as AssetType,
    quantity: "",
    averageCostPrice: "",
    currentPrice: "",
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      const holding = await addOrUpdateHolding({
        user: { id: userId },
        symbol: form.symbol.toUpperCase(),
        assetName: form.assetName,
        assetType: form.assetType,
        quantity: Number(form.quantity),
        averageCostPrice: Number(form.averageCostPrice),
        currentPrice: Number(form.currentPrice),
      });
      setMessage(`Holding added/updated for ${holding.symbol}`);
      onAdded?.(holding);
      setForm({ symbol: "", assetName: "", assetType: "STOCK", quantity: "", averageCostPrice: "", currentPrice: "" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to add holding");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <PlusCircle className="h-5 w-5 text-emerald-400" />
          Add / Update Holding
        </CardTitle>
        <CardDescription>Update the portfolio for the current user.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <Input
            placeholder="Symbol (e.g. AAPL)"
            value={form.symbol}
            onChange={(e) => setForm({ ...form, symbol: e.target.value })}
            required
          />
          <Input
            placeholder="Asset name"
            value={form.assetName}
            onChange={(e) => setForm({ ...form, assetName: e.target.value })}
            required
          />
          <Select
            value={form.assetType}
            onChange={(e) => setForm({ ...form, assetType: e.target.value as AssetType })}
          >
            {ASSET_TYPES.map((type) => (
              <option key={type} value={type}>
                {type.replace(/_/g, " ")}
              </option>
            ))}
          </Select>
          <Input
            placeholder="Quantity"
            type="number"
            step="any"
            value={form.quantity}
            onChange={(e) => setForm({ ...form, quantity: e.target.value })}
            required
          />
          <Input
            placeholder="Average cost price"
            type="number"
            step="any"
            value={form.averageCostPrice}
            onChange={(e) => setForm({ ...form, averageCostPrice: e.target.value })}
            required
          />
          <Input
            placeholder="Current price"
            type="number"
            step="any"
            value={form.currentPrice}
            onChange={(e) => setForm({ ...form, currentPrice: e.target.value })}
            required
          />
          <div className="sm:col-span-2 lg:col-span-3">
            <Button type="submit" disabled={loading} className="w-full sm:w-auto">
              {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <PlusCircle className="mr-2 h-4 w-4" />}
              Save Holding
            </Button>
          </div>
          {message && (
            <div className="sm:col-span-2 lg:col-span-3">
              <Alert variant="success">
                <AlertTitle>Saved</AlertTitle>
                <AlertDescription>{message}</AlertDescription>
              </Alert>
            </div>
          )}
          {error && (
            <div className="sm:col-span-2 lg:col-span-3">
              <Alert variant="destructive">
                <AlertTitle>Error</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
}

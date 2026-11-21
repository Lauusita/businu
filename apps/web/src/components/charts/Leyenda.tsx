export function Leyenda({ items }: { items: { nombre: string; color: string }[] }) {
  return (
    <div className="flex flex-wrap gap-x-4 gap-y-1">
      {items.map((i) => (
        <span key={i.nombre} className="inline-flex items-center gap-1.5 text-[12px] text-texto-2">
          <span className="size-2 rounded-full" style={{ background: i.color }} aria-hidden />
          {i.nombre}
        </span>
      ))}
    </div>
  )
}

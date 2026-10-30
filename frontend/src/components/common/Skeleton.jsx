import React from 'react';

export const Skeleton = ({
  variant = 'text',
  width,
  height,
  className = '',
  count = 1,
}) => {
  const base = 'animate-pulse bg-slate-200/80 rounded';

  const variants = {
    text: 'h-4 w-full rounded',
    circular: 'rounded-full aspect-square',
    rectangular: 'w-full h-24 rounded-lg',
  };

  const items = Array.from({ length: count });

  return (
    <>
      {items.map((_, i) => (
        <div
          key={i}
          aria-hidden="true"
          style={{ width, height }}
          className={`${base} ${variants[variant] || variants.text} ${className}`}
        />
      ))}
    </>
  );
};

export default Skeleton;

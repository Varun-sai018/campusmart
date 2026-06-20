import { BookOpen, Cpu, Monitor, ShoppingBag, Coffee, HeartHandshake } from 'lucide-react';

const iconMap = {
  Electronics: Monitor,
  Apparel: ShoppingBag,
  Books: BookOpen,
  'Study Gear': Cpu,
  Lifestyle: Coffee,
  Wellness: HeartHandshake,
};

export default function CategoryGrid({ categories, selectedCategory, onSelect }) {
  return (
    <div className="category-grid">
      {categories.map((category) => {
        const Icon = iconMap[category.name] || ShoppingBag;
        const active = selectedCategory === category.id;

        return (
          <button
            key={category.id}
            type="button"
            className={`category-card ${active ? 'category-card-active' : ''}`}
            onClick={() => onSelect(category)}
          >
            <span className="category-icon">
              <Icon size={20} />
            </span>
            <div>
              <h3>{category.name}</h3>
              <p>{category.description || 'Discover curated student picks'}</p>
            </div>
          </button>
        );
      })}
    </div>
  );
}

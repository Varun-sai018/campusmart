import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowRight, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';

const slides = [
  {
    title: 'Shop student essentials with confidence',
    subtitle: 'Fresh campus deals, fast delivery, and trusted seller ratings.',
    cta: 'Browse marketplace',
    tag: 'Campus favourites',
  },
  {
    title: 'Upgrade your study setup',
    subtitle: 'Find premium laptops, accessories, and stationery for every budget.',
    cta: 'See trending products',
    tag: 'Top tech picks',
  },
  {
    title: 'Sell smarter with CampusMart',
    subtitle: 'List your products quickly and connect with student buyers.',
    cta: 'Manage your storefront',
    tag: 'Seller spotlight',
  },
];

export default function HeroBanner() {
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      setActiveIndex((current) => (current + 1) % slides.length);
    }, 6000);
    return () => window.clearInterval(intervalId);
  }, []);

  const slide = slides[activeIndex];

  return (
    <section className="hero-banner market-hero">
      <div className="hero-banner-inner">
        <div className="hero-copy">
          <span className="hero-tag">
            <Sparkles size={18} /> {slide.tag}
          </span>
          <AnimatePresence mode="wait">
            <motion.div
              key={slide.title}
              initial={{ opacity: 0, y: 24 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -24 }}
              transition={{ duration: 0.5 }}
            >
              <h1>{slide.title}</h1>
              <p>{slide.subtitle}</p>
              <div className="hero-actions">
                <Link className="market-btn market-btn-primary" to="/">
                  {slide.cta} <ArrowRight size={16} />
                </Link>
              </div>
            </motion.div>
          </AnimatePresence>
        </div>
        <div className="hero-preview">
          <div className="hero-preview-card">
            <span>CampusMart picks</span>
            <p>Curated collections for campus life, study, and gifting.</p>
            <div className="hero-preview-stat">
              <strong>95%</strong>
              <span>Seller satisfaction</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

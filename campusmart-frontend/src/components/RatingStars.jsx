function RatingStars({ rating = 0 }) {
  const normalizedRating = Math.min(Math.max(Math.round(rating), 0), 5);
  const stars = Array.from({ length: 5 }, (_, index) => (
    <span key={index} className="me-1 text-warning" aria-hidden="true">
      {index < normalizedRating ? '★' : '☆'}
    </span>
  ));

  return <div>{stars}</div>;
}

export default RatingStars;

function ReviewList({ reviews }) {
  if (!reviews || reviews.length === 0) {
    return <p className="text-muted mb-0">No reviews available for this product yet.</p>;
  }

  return (
    <div className="list-group">
      {reviews.map((review) => (
        <div key={review.id} className="list-group-item border-0 px-0 pb-3 mb-3 bg-transparent">
          <div className="d-flex justify-content-between align-items-start mb-2">
            <div>
              <h4 className="h6 mb-1">{review.buyerName || 'Anonymous'}</h4>
              <div className="small text-muted">{new Date(review.createdAt).toLocaleDateString()}</div>
            </div>
            <div className="text-warning">
              {Array.from({ length: 5 }, (_, index) => (
                <span key={index}>{index < review.rating ? '★' : '☆'}</span>
              ))}
            </div>
          </div>
          <p className="mb-0 text-muted">{review.comment || 'No comment provided.'}</p>
        </div>
      ))}
    </div>
  );
}

export default ReviewList;

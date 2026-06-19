import { useEffect, useState } from 'react';

const conditionOptions = ['NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR'];
const statusOptions = ['AVAILABLE', 'RESERVED', 'SOLD'];

function SellerProductForm({ product, categories, onSubmit, submitLabel, showStatus = false, loading }) {
  const [title, setTitle] = useState(product?.title || '');
  const [description, setDescription] = useState(product?.description || '');
  const [price, setPrice] = useState(product?.price ?? '');
  const [condition, setCondition] = useState(product?.condition || 'GOOD');
  const [status, setStatus] = useState(product?.status || 'AVAILABLE');
  const [categoryId, setCategoryId] = useState(product?.categoryId || '');

  useEffect(() => {
    setTitle(product?.title || '');
    setDescription(product?.description || '');
    setPrice(product?.price ?? '');
    setCondition(product?.condition || 'GOOD');
    setStatus(product?.status || 'AVAILABLE');
    setCategoryId(product?.categoryId || '');
  }, [product]);

  const handleSubmit = (event) => {
    event.preventDefault();

    const payload = {
      title: title.trim(),
      description: description.trim(),
      price: price === '' ? null : Number(price),
      condition,
      categoryId: Number(categoryId),
    };

    if (showStatus) {
      payload.status = status;
    }

    onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className="card shadow-sm p-4">
      <div className="row g-3">
        <div className="col-12">
          <label className="form-label">Product Title</label>
          <input
            type="text"
            className="form-control"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Enter product title"
            required
          />
        </div>

        <div className="col-12">
          <label className="form-label">Description</label>
          <textarea
            className="form-control"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            placeholder="Describe the item"
          />
        </div>

        <div className="col-md-6">
          <label className="form-label">Price (USD)</label>
          <input
            type="number"
            className="form-control"
            min="0.01"
            step="0.01"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            placeholder="0.00"
            required
          />
        </div>

        <div className="col-md-6">
          <label className="form-label">Condition</label>
          <select className="form-select" value={condition} onChange={(e) => setCondition(e.target.value)} required>
            {conditionOptions.map((option) => (
              <option key={option} value={option}>
                {option.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>

        {showStatus && (
          <div className="col-md-6">
            <label className="form-label">Status</label>
            <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)} required>
              {statusOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="col-md-6">
          <label className="form-label">Category</label>
          <select
            className="form-select"
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            required
          >
            <option value="" disabled>
              Select a category
            </option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="col-12 d-flex justify-content-end gap-2">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Saving...' : submitLabel}
          </button>
        </div>
      </div>
    </form>
  );
}

export default SellerProductForm;

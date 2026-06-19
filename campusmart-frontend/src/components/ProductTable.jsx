function ProductTable({ products, onEdit, onDelete }) {
  return (
    <div className="table-responsive shadow-sm rounded-3 bg-white">
      <table className="table table-hover mb-0">
        <thead className="table-light">
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Condition</th>
            <th>Status</th>
            <th>Price</th>
            <th className="text-end">Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id}>
              <td>{product.title}</td>
              <td>{product.categoryName}</td>
              <td>{product.condition}</td>
              <td>{product.status}</td>
              <td>${Number(product.price).toFixed(2)}</td>
              <td className="text-end">
                <button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(product.id)}>
                  Edit
                </button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(product.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ProductTable;

class BoundedQueue<T> {
  final int capacity;
  final List<T> _items = [];

  BoundedQueue(this.capacity);

  void add(T item) {
    if (_items.length >= capacity) {
      _items.removeAt(0);
    }
    _items.add(item);
  }

  List<T> get items => List.unmodifiable(_items);

  @override
  String toString() => _items.toString();
}
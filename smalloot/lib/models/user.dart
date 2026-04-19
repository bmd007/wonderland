class User {
  final String id;
  final String name;
  final String email;
  final String avatarUrl;
  final double balance;

  const User({
    required this.id,
    required this.name,
    required this.email,
    this.avatarUrl = '',
    this.balance = 0,
  });

  User copyWith({
    String? id,
    String? name,
    String? email,
    String? avatarUrl,
    double? balance,
  }) {
    return User(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      balance: balance ?? this.balance,
    );
  }
}

export const getInitials = (email: string): string => {
  return email.substring(0, 2).toUpperCase();
};

export const getAvatarColor = (email: string): string => {
  const colors = [
    'bg-red-500',
    'bg-yellow-500',
    'bg-green-500',
    'bg-blue-500',
    'bg-indigo-500',
    'bg-purple-500',
  ];
  let hash = 0;
  for (let i = 0; i < email.length; i++) {
    hash = email.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
};

export const truncateEmail = (email: string, maxLength: number = 30): string => {
  if (email.length <= maxLength) return email;
  const atIndex = email.indexOf('@');
  if (atIndex < maxLength - 8) {
    return email.substring(0, maxLength - 3) + '...';
  }
  return email.substring(0, maxLength - 3) + '...';
};

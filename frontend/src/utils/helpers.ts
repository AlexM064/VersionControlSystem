export const formatDate = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatDateOnly = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

export const formatTimeOnly = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  });
};

export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

export const formatStatusBadge = (status: string): string => {
  return status
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
};

export const getDiff = (oldText: string, newText: string): string[] => {
  const oldLines = oldText.split('\n');
  const newLines = newText.split('\n');
  const result: string[] = [];

  const maxLength = Math.max(oldLines.length, newLines.length);

  for (let i = 0; i < maxLength; i++) {
    const oldLine = oldLines[i] || '';
    const newLine = newLines[i] || '';

    if (oldLine !== newLine) {
      if (oldLine) result.push(`- ${oldLine}`);
      if (newLine) result.push(`+ ${newLine}`);
    } else if (oldLine) {
      result.push(`  ${oldLine}`);
    }
  }

  return result;
};

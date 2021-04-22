/* determine if the number of values recorded into the array will be even or odd */
double getMedian(double arr[], int n) {
  int x = n % 2 == 0 ? n / 2 : n / 2 + 1;
  return qselect(arr, 0, n - 1, x - 1);
}

/* sorting the array into ascending order and returning the median value */
double qselect(double A[], int start, int end, int k) {
  if (start == end) {
    return A[start];
  }
  
  int left = start, right = end;
  int index = (left + right) / 2;
  double pivot = A[index];

  while (left <= right) {
    while (left <= right && A[left] > pivot) {
      left++;
    }

    while (left <= right && A[right] < pivot) {
      right--;
    }

    if (left <= right) {
      double tmp = A[left];
      A[left] = A[right];
      A[right] = tmp;

      left++;
      right--;
    }
  }

  if (k >= start && k <= right) {
    return qselect(A, start, right, k);
  }
  if (k >= left && k <= end) {
    return qselect(A, left, end, k);
  }
  return A[right + 1];
}

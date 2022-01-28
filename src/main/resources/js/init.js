function validateMatrix(data) {
   if (!Array.isArray(data)) {
      throw "data is not an array";
   }
   for (e of data) {
      if (!Array.isArray(e)) {
         throw "This array is not a 2d array but either one dimensional or complex";
      }
      for (sub of e) {
         if (Array.isArray(sub)) {
            throw "This array is not a 2d array but have more than 2 dimensions";
         }
      }
   }
   let maxSize = data.reduce((x, y) => Math.max(x, y.length), 0);
   let minSize = data.reduce((x, y) => Math.min(x, y.length), maxSize);
   if (maxSize !== minSize) {
      throw "data rows is not of equal size: longest row is " + maxSize + " but shortest row is " + minSize  ;
   }
}

function View(data, title='js[][]') {
   validateMatrix(data);
   inout.View(Java.to(data,"java.lang.Object[][]"), title);
}

function transpose(matrix) {
   validateMatrix(matrix);
   const rows = matrix.length;
   const cols = matrix[0].length;
   const grid = [];
   for (let j = 0; j < cols; j++) {
      grid[j] = Array(rows);
   }
   for (let i = 0; i < rows; i++) {
      for (let j = 0; j < cols; j++) {
         grid[j][i] = matrix[i][j];
      }
   }
   return grid;
}

View([
  [1,2,3,4,5],
  ['a', 'b', 'cd', 'efg', 'åäö'],
  [true, false, NaN, null, undefined]
], "horizontal");

View(transpose([
  [1,2,3,4,5],
  ['a', 'b', 'cd', 'efg', 'åäö'],
  [true, false, NaN, null, undefined]
]), "vertical");

View(transpose([[3, 8], [1,2]]));

let arr = [
  [1,2,3,4],
  ['a', 'b', 'cd', 'efg'],
  [true, true, false, null],
  ['gree', 'yellow', NaN, null]
];
inout.View(Java.to(arr,"java.lang.Object[][]"));
inout.View(arr);
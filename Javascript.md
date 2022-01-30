# Javascript support in Ride

Ride uses Nashorn to execute javascript code. Nashorn is initiated with:
1. "--language=es6" meaning there is some support for ES 6 (though not 100%)
2. Adding two functions ta make it easier to handle tabular (2d array) data
   1. transpose which transposes all columns of a 2d array into rows
   2. View which allows you to view the content of a 2d array in the Ride "View" tab.

Here are some simple examples:
```javascript

View([
[1,2,3,4,5],
['a', 'b', 'cd', 'efg', 'åäö'],
[true, false, NaN, null, undefined]
], "horizontal");
```

Which will look similar to this:

| V0  | V1  | V2  | V3  | V4  |
| --- | --- | --- | --- | --- |
| 1   | 2   | 3   | 4   | 5   |
| a   | b   | cd  | efg | åäö |
|true |false| NaN | null|undefined |


Using `transpose` you can "flip" it e.g:

```javascript
View(transpose([
[1,2,3,4,5],
['a', 'b', 'cd', 'efg', 'åäö'],
[true, false, NaN, null, undefined]
]), "vertical");
```

Which will look similar to this:

| V0  | V1  | V2        |
| --- | --- | ---       | 
| 1   | a   | true      | 
| 2   | b   | false     | 
| 3   | cd  | NaN       | 
| 4   | efg | null      | 
| 5   | åäö | undefined | 

See https://docs.oracle.com/en/java/javase/11/scripting/index.html for details of what you can do with the Nashorn 
Javascript engine.
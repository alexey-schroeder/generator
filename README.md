# generator

category C0(третье число, в середине) встречается в 62%\
если category C0=1, то в первом и в втором столбце стоят позитивные индексы, например:
2, 1, 1, 0, 0 или 3, 1, 1, 0, -1

Индексы симметричны только в 7.8% случаев, т.е. очень редко.\
 Пример: 3, 2, 1, 2, 3 -> редкость
 
 если есть негативный индекс, то справа от него почти всегда негативные индексы, иногда 0, и почти никогда позитивный индекс\
 Пример:\
   1, 0, -1, -1, -2 ->часто\
   1, 0, -1, 0, -1 -> иногда\
   1, 0, -1, 1, 1 -> почти никогда
   
   если есть три и более негативных индекса, то остальные индексы почти всегда равны 0\
   пример:\
   0, -1, -2, -2, -3\
   0, -1, -1, -1, 0\
   0, -1, 0, -2, -2   

в первом ряду 0 выпадает не более пяти(5) 0-индексов вподряд\
пример:\
1, 0, 0, -2, -1 -> теперь снова не 1\
0, -1, 0, 0, 0 -> 0\
0, 0, 0, -2, 0 -> 0\
0, 3, 2, 1, 0 -> 0\
0, 1, 0, -1, 0 -> 0\
0, 0, 0, 1, 0 -> 0\
1, 3, 2, 1, 0 -> 1\


-- половина чисел четная/нечетная 1,3,13, 22, 46\
-- количество чисел с разницей в 1, т.е. последовательные числа. Пример> 2,3, 23,24, 49\
-- среднее арифметическое? оно по идее, не может быть слишком маленьким и слишком большим\
-- половина чисел простые?\
-- были ли числа в последних х розыгрышах\
-- одинаковая разница между числами. Пример> 1, 11, 21, 31, 41\
-- были ли например 3 числа в предыдущих выигрышных вариантах. Пример> раьше был выигрыш> 3, 7, 10, 14, 48. а теперь тестируется комбинация> 3, 7, 10, 22, 37 - первые три числа уже были когда то в выигрыше\
-- разница между самым маленьким и самым большим числом пример> 1, 5, 18, 33, 49 > 49 - 1 = 48, не слишком много?\
-- самая большая разница между соседними числами. Пример> 1,3, 34, 41, 48 > самая большая разница 34 - 3 = 31. Не слишком много?\

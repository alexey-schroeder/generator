# generator

category C0(третье число, в середине) встречается в 62%\
если category C0=1, то в первом и в втором столбце стоят позитивные индексы, например:
2, 1, 1, 0, 0 или 3, 1, 1, 0, -1

Индексы симметричны только в 7.8% случаев, т.е. очень редко.\
 Пример: 3, 2, 1, 2, 3 -> редкость\
 
 если есть негативный индекс, то слева от него почти всегда негативные индексы, иногда 0, и почти никогда позитивный индекс\
 Пример:\
   1, 0, -1, -1, -2 ->часто\
   1, 0, -1, 0, -1 -> иногда\
   1, 0, -1, 1, 1 -> почти никогда\
   
   если есть три и более негативных индекса, то остальные индексы почти всегда равны 0\
   пример:\
   0, -1, -2, -2, -3\
   0, -1, -1, -1, 0\
   0, -1, 0, -2, -2\   

в первом ряду 0 выпадает не более пяти(5) 0-индексов вподряд\
пример:\
1, 0, 0, -2, -1 -> теперь снова не 1\
0, -1, 0, 0, 0 -> 0\
0, 0, 0, -2, 0 -> 0\
0, 3, 2, 1, 0 -> 0\
0, 1, 0, -1, 0 -> 0\
0, 0, 0, 1, 0 -> 0\
1, 3, 2, 1, 0 -> 1
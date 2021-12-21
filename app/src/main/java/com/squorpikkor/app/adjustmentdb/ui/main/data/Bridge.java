package com.squorpikkor.app.adjustmentdb.ui.main.data;

/** Перевод nameId -> name при получении данных из БД и перевод name -> nameId при отправке данных
 * в БД. После получении данных приложение работает только со значениями name.
 * Другими словами: приложение ничего не знает о nameId, класс FireBaseHelper ничего не знает о name,
 * общаются друг с другом через Bridge, это что-то типа переводчика*/
public class Bridge {



}

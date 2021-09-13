package com.lapluma.knowledg.data.HomeData;

import android.os.Bundle;

import com.lapluma.knowledg.data.BasePresenter;
import com.lapluma.knowledg.data.BaseView;
import com.lapluma.knowledg.model.ListItem;

import java.util.List;

public interface ItemListContract {

    interface View extends BaseView<Presenter> {
        // focus on the functions to monitor on view-model

        void setItemList(List<ListItem> list);
        // set the itemList

        void appendItemList(List<ListItem> list);
        // append list to the end

        void resetItemRead(int pos, boolean has_read);
        // reset the item on pos to be has_read

        void onSuccess(boolean loadCompleted);
        // on receive success response

        void onError();
        // on receive error response
    }

    interface Presenter extends BasePresenter {
        // focus on the functions to be used by data

        boolean isLoading();
        // check the state

        void requireMoreItems();
        // request for more items

        void refreshItems();
        // refresh the items

        void openItemDetail(ListItem item, Bundle options);
        // jump to the detail page

        void fetchItemRead(int pos);
        // still don't know the usage

        void setKeyword(String keyword);
        // set the keyword
    }
}


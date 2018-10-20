package in.co.theshipper.www.shipper_driver.Utils;

import android.widget.AbsListView;

import in.co.theshipper.www.shipper_driver.Helper;

public abstract class EndlessScrollListener implements AbsListView.OnScrollListener{

    private int currentVisibleItemCount;
    private int currentScrollState;
    private int currentFirstVisibleItem;
    private int totalItem;

    // before loading more.
    private int visibleThreshold = 5;

    // The current offset index of data you have loaded
    private int currentPage = 0;

    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;

    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;

    // Sets the starting page index
    private int startingPageIndex = 0;

    public EndlessScrollListener() {
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    public EndlessScrollListener(int visibleThreshold, int startPage) {
        this.visibleThreshold = visibleThreshold;
        this.startingPageIndex = startPage;
        this.currentPage = startPage;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        this.currentScrollState = scrollState;
        this.isScrollCompleted();

    }

    private void isScrollCompleted() {

        if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                && this.currentScrollState == SCROLL_STATE_IDLE) {

            this.currentPage++;
            onLoadMore(currentPage);

        }

    }

    public abstract void onLoadMore(int page);

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        this.totalItem = totalItemCount;

    }

}

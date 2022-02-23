package sdk.interfaces;

/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public interface InquireResponse<CPayInquireResult>
{
    public void gotInquireResult(CPayInquireResult object);
}
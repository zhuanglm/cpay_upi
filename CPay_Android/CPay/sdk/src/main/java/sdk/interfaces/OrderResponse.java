package sdk.interfaces;

/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public interface OrderResponse<CPayOrderResult>
{
    public void gotOrderResult(CPayOrderResult object);
}
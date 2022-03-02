package upisdk.interfaces;

public interface OrderResponse<CPayUPIOrderResult>
{
    public void gotOrderResult(CPayUPIOrderResult object);
}
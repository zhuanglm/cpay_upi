package upisdk.interfaces;

public interface InquireResponse<CPayUPIInquireResult>
{
    public void gotInquireResult(CPayUPIInquireResult object);
}
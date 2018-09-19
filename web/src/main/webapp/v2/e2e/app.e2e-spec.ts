import { NpuPage } from './app.po';

describe('npu App', function() {
  let page: NpuPage;

  beforeEach(() => {
    page = new NpuPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});

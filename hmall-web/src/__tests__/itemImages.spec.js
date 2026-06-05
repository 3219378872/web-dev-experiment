import { describe, it, expect } from 'vitest';
import { parseItemImages, mainImage } from '@/utils/itemImages';

describe('parseItemImages', () => {
  it('解析单图', () => {
    expect(parseItemImages('http://a/1.png')).toEqual(['http://a/1.png']);
  });

  it('解析逗号分隔多图并去除空白', () => {
    expect(parseItemImages(' a.png , b.png ,c.png')).toEqual(['a.png', 'b.png', 'c.png']);
  });

  it('去重重复图片', () => {
    expect(parseItemImages('a.png,a.png,b.png')).toEqual(['a.png', 'b.png']);
  });

  it('空 / null / 非字符串返回空数组', () => {
    expect(parseItemImages('')).toEqual([]);
    expect(parseItemImages(null)).toEqual([]);
    expect(parseItemImages(undefined)).toEqual([]);
    expect(parseItemImages(123)).toEqual([]);
  });

  it('全是逗号/空白返回空数组', () => {
    expect(parseItemImages(' , , ')).toEqual([]);
  });
});

describe('mainImage', () => {
  it('返回第一张图', () => {
    expect(mainImage(['a.png', 'b.png'])).toBe('a.png');
  });
  it('空列表返回 null', () => {
    expect(mainImage([])).toBeNull();
    expect(mainImage(undefined)).toBeNull();
  });
});
